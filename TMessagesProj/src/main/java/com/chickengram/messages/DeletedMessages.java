package com.chickengram.messages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;

import androidx.collection.LongSparseArray;

import com.chickengram.ChickengramConfig;

import org.telegram.SQLite.SQLiteCursor;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public final class DeletedMessages {

    public static final String LABEL = "удалено";

    private static final String DATABASE = "chickengram_deleted.db";
    private static final String TABLE = "deleted";
    private static final int VERSION = 1;
    private static final int BADGE_COLOR = 0xCCD7261B;

    private static final Object lock = new Object();
    private static final HashMap<Integer, HashMap<Long, HashSet<Integer>>> cache = new HashMap<>();
    private static Helper helper;
    private static TextPaint badgeText;
    private static Paint badgePaint;
    private static final RectF badgeRect = new RectF();

    private DeletedMessages() {
    }

    public static boolean keepDeleted() {
        return ChickengramConfig.keepDeletedMessages();
    }

    public static void remember(int account, LongSparseArray<ArrayList<Integer>> deleted, MessagesStorage storage) {
        final int date = (int) (System.currentTimeMillis() / 1000);
        synchronized (lock) {
            final HashMap<Long, HashSet<Integer>> accountCache = load(account);
            SQLiteDatabase database = null;
            try {
                database = helper().getWritableDatabase();
                database.beginTransaction();
                for (int i = 0; i < deleted.size(); i++) {
                    final long dialog = deleted.keyAt(i);
                    final ArrayList<Integer> ids = deleted.valueAt(i);
                    if (ids == null) {
                        continue;
                    }
                    HashSet<Integer> set = accountCache.get(dialog);
                    if (set == null) {
                        set = new HashSet<>();
                        accountCache.put(dialog, set);
                    }
                    for (Integer id : ids) {
                        if (id == null || !set.add(id)) {
                            continue;
                        }
                        final ContentValues values = new ContentValues();
                        values.put("account", account);
                        values.put("dialog", dialog);
                        values.put("mid", id);
                        values.put("uid", dialog);
                        values.put("date", date);
                        database.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                    }
                }
                database.setTransactionSuccessful();
            } catch (Exception e) {
                FileLog.e(e);
            } finally {
                endTransaction(database);
            }
        }
        for (int i = 0; i < deleted.size(); i++) {
            final long dialog = deleted.keyAt(i);
            final ArrayList<Integer> ids = deleted.valueAt(i);
            if (ids == null || ids.isEmpty()) {
                continue;
            }
            AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(account)
                .postNotificationName(NotificationCenter.chickengramMessagesDeleted, dialog, ids));
            if (storage != null) {
                final ArrayList<Integer> copy = new ArrayList<>(ids);
                storage.getStorageQueue().postRunnable(() -> saveCopies(account, dialog, copy, storage));
            }
        }
    }

    private static void saveCopies(int account, long dialog, ArrayList<Integer> ids, MessagesStorage storage) {
        final String in = TextUtils.join(",", ids);
        final String query = dialog != 0
            ? String.format(Locale.US, "SELECT mid, uid, data FROM messages_v2 WHERE uid = %d AND mid IN(%s)", dialog, in)
            : String.format(Locale.US, "SELECT mid, uid, data FROM messages_v2 WHERE is_channel = 0 AND mid IN(%s)", in);
        SQLiteCursor cursor = null;
        final ArrayList<ContentValues> rows = new ArrayList<>();
        try {
            cursor = storage.getDatabase().queryFinalized(query);
            while (cursor.next()) {
                final ContentValues values = new ContentValues();
                values.put("uid", cursor.longValue(1));
                values.put("data", cursor.byteArrayValue(2));
                values.put("mid", cursor.intValue(0));
                rows.add(values);
            }
        } catch (Exception e) {
            FileLog.e(e);
        } finally {
            if (cursor != null) {
                cursor.dispose();
            }
        }
        if (rows.isEmpty()) {
            return;
        }
        synchronized (lock) {
            SQLiteDatabase database = null;
            try {
                database = helper().getWritableDatabase();
                database.beginTransaction();
                for (ContentValues values : rows) {
                    final int mid = values.getAsInteger("mid");
                    values.remove("mid");
                    database.update(
                        TABLE,
                        values,
                        "account = ? AND dialog = ? AND mid = ?",
                        new String[] { String.valueOf(account), String.valueOf(dialog), String.valueOf(mid) });
                }
                database.setTransactionSuccessful();
            } catch (Exception e) {
                FileLog.e(e);
            } finally {
                endTransaction(database);
            }
        }
    }

    public static boolean isDeleted(int account, MessageObject object) {
        if (object == null || object.messageOwner == null || object.getId() <= 0) {
            return false;
        }
        final long dialog = dialogKey(object);
        synchronized (lock) {
            final HashSet<Integer> set = load(account).get(dialog);
            return set != null && set.contains(object.getId());
        }
    }

    public static long dialogKey(MessageObject object) {
        final TLRPC.Peer peer = object.messageOwner.peer_id;
        return peer != null && peer.channel_id != 0 ? -peer.channel_id : 0;
    }

    public static int count() {
        synchronized (lock) {
            try {
                return (int) android.database.DatabaseUtils.queryNumEntries(helper().getReadableDatabase(), TABLE);
            } catch (Exception e) {
                FileLog.e(e);
                return 0;
            }
        }
    }

    public static File exportDatabase(File directory) throws Exception {
        synchronized (lock) {
            if (helper != null) {
                helper.close();
                helper = null;
            }
            final File source = ApplicationLoader.applicationContext.getDatabasePath(DATABASE);
            final File target = new File(directory, "chickengram-deleted-" + System.currentTimeMillis() + ".db");
            if (source.exists()) {
                copy(source, target);
            } else {
                helper().getWritableDatabase();
                helper.close();
                helper = null;
                copy(source, target);
            }
            return target;
        }
    }

    public static int importDatabase(File file) {
        int imported = 0;
        SQLiteDatabase input = null;
        Cursor cursor = null;
        synchronized (lock) {
            SQLiteDatabase database = null;
            try {
                input = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY);
                cursor = input.query(TABLE, new String[] { "account", "dialog", "mid", "uid", "date", "data" }, null, null, null, null, null);
                database = helper().getWritableDatabase();
                database.beginTransaction();
                while (cursor.moveToNext()) {
                    final ContentValues values = new ContentValues();
                    values.put("account", cursor.getInt(0));
                    values.put("dialog", cursor.getLong(1));
                    values.put("mid", cursor.getInt(2));
                    values.put("uid", cursor.getLong(3));
                    values.put("date", cursor.getInt(4));
                    if (!cursor.isNull(5)) {
                        values.put("data", cursor.getBlob(5));
                    }
                    if (database.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1) {
                        imported++;
                    }
                }
                database.setTransactionSuccessful();
            } catch (Exception e) {
                FileLog.e(e);
                imported = -1;
            } finally {
                endTransaction(database);
                if (cursor != null) {
                    cursor.close();
                }
                if (input != null) {
                    input.close();
                }
                cache.clear();
            }
        }
        return imported;
    }

    public static void drawBadge(Canvas canvas, float x, float y) {
        if (badgeText == null) {
            badgeText = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            badgeText.setColor(0xFFFFFFFF);
            badgeText.setTypeface(AndroidUtilities.bold());
            badgeText.setTextSize(AndroidUtilities.dp(10));
            badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            badgePaint.setColor(BADGE_COLOR);
        }
        final float padding = AndroidUtilities.dp(5);
        final float height = AndroidUtilities.dp(16);
        final float left = x + AndroidUtilities.dp(4);
        final float top = y + AndroidUtilities.dp(4);
        badgeRect.set(left, top, left + badgeText.measureText(LABEL) + padding * 2, top + height);
        canvas.drawRoundRect(badgeRect, height / 2f, height / 2f, badgePaint);
        final float baseline = badgeRect.centerY() - (badgeText.descent() + badgeText.ascent()) / 2f;
        canvas.drawText(LABEL, left + padding, baseline, badgeText);
    }

    private static HashMap<Long, HashSet<Integer>> load(int account) {
        HashMap<Long, HashSet<Integer>> result = cache.get(account);
        if (result != null) {
            return result;
        }
        result = new HashMap<>();
        cache.put(account, result);
        Cursor cursor = null;
        try {
            cursor = helper().getReadableDatabase().query(
                TABLE,
                new String[] { "dialog", "mid" },
                "account = ?",
                new String[] { String.valueOf(account) },
                null, null, null);
            while (cursor.moveToNext()) {
                final long dialog = cursor.getLong(0);
                HashSet<Integer> set = result.get(dialog);
                if (set == null) {
                    set = new HashSet<>();
                    result.put(dialog, set);
                }
                set.add(cursor.getInt(1));
            }
        } catch (Exception e) {
            FileLog.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    private static void endTransaction(SQLiteDatabase database) {
        if (database == null) {
            return;
        }
        try {
            database.endTransaction();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private static void copy(File source, File target) throws Exception {
        try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(target)) {
            final byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
        }
    }

    private static Helper helper() {
        if (helper == null) {
            helper = new Helper(ApplicationLoader.applicationContext);
        }
        return helper;
    }

    private static final class Helper extends SQLiteOpenHelper {

        Helper(Context context) {
            super(context, DATABASE, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL("CREATE TABLE " + TABLE + " ("
                + "account INTEGER NOT NULL, "
                + "dialog INTEGER NOT NULL, "
                + "mid INTEGER NOT NULL, "
                + "uid INTEGER NOT NULL, "
                + "date INTEGER NOT NULL, "
                + "data BLOB, "
                + "PRIMARY KEY (account, dialog, mid))");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        }
    }
}
