package com.chickengram.messages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.chickengram.ChickengramConfig;

import org.telegram.SQLite.SQLiteCursor;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public final class EditHistory {

    public static final class Version {
        public final int date;
        public final String text;

        Version(int date, String text) {
            this.date = date;
            this.text = text;
        }
    }

    private static final String DATABASE = "chickengram_edits.db";
    private static final String TABLE = "edits";
    private static final int VERSION = 1;

    private static final Object lock = new Object();
    private static final HashMap<Integer, HashSet<String>> cache = new HashMap<>();
    private static Helper helper;

    private EditHistory() {
    }

    public static void remember(int account, TLRPC.Message message, MessagesStorage storage) {
        if (message == null || storage == null || !ChickengramConfig.saveEditHistory()) {
            return;
        }
        final long dialog = MessageObject.getDialogId(message);
        final int mid = message.id;
        final String newText = message.message == null ? "" : message.message;
        if (dialog == 0 || mid <= 0) {
            return;
        }
        storage.getStorageQueue().postRunnable(() -> {
            TLRPC.Message old = null;
            SQLiteCursor cursor = null;
            try {
                cursor = storage.getDatabase().queryFinalized(String.format(Locale.US, "SELECT data FROM messages_v2 WHERE uid = %d AND mid = %d", dialog, mid));
                if (cursor.next()) {
                    final NativeByteBuffer data = cursor.byteBufferValue(0);
                    if (data != null) {
                        old = TLRPC.Message.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            } finally {
                if (cursor != null) {
                    cursor.dispose();
                }
            }
            if (old == null || TextUtils.isEmpty(old.message) || TextUtils.equals(old.message, newText)) {
                return;
            }
            store(account, dialog, mid, old.edit_date != 0 ? old.edit_date : old.date, old.message);
        });
    }

    public static void rememberOwn(int account, TLRPC.Message message, String oldText, String newText) {
        if (message == null || !ChickengramConfig.saveEditHistory() || TextUtils.isEmpty(oldText) || TextUtils.equals(oldText, newText)) {
            return;
        }
        final long dialog = MessageObject.getDialogId(message);
        final int mid = message.id;
        if (dialog == 0 || mid <= 0) {
            return;
        }
        final int date = message.edit_date != 0 ? message.edit_date : message.date;
        Utilities.globalQueue.postRunnable(() -> store(account, dialog, mid, date, oldText));
    }

    private static void store(int account, long dialog, int mid, int date, String text) {
        synchronized (lock) {
            try {
                final ContentValues values = new ContentValues();
                values.put("account", account);
                values.put("dialog", dialog);
                values.put("mid", mid);
                values.put("date", date);
                values.put("text", text);
                helper().getWritableDatabase().insert(TABLE, null, values);
                load(account).add(key(dialog, mid));
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public static boolean has(int account, MessageObject object) {
        if (object == null || object.messageOwner == null || object.getId() <= 0) {
            return false;
        }
        synchronized (lock) {
            return load(account).contains(key(object.getDialogId(), object.getId()));
        }
    }

    public static ArrayList<Version> get(int account, long dialog, int mid) {
        final ArrayList<Version> result = new ArrayList<>();
        synchronized (lock) {
            Cursor cursor = null;
            try {
                cursor = helper().getReadableDatabase().query(
                    TABLE,
                    new String[] { "date", "text" },
                    "account = ? AND dialog = ? AND mid = ?",
                    new String[] { String.valueOf(account), String.valueOf(dialog), String.valueOf(mid) },
                    null, null, "date ASC, rowid ASC");
                while (cursor.moveToNext()) {
                    result.add(new Version(cursor.getInt(0), cursor.getString(1)));
                }
            } catch (Exception e) {
                FileLog.e(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return result;
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

    public static void clear() {
        synchronized (lock) {
            try {
                helper().getWritableDatabase().delete(TABLE, null, null);
            } catch (Exception e) {
                FileLog.e(e);
            }
            cache.clear();
        }
    }

    private static String key(long dialog, int mid) {
        return dialog + "_" + mid;
    }

    private static HashSet<String> load(int account) {
        HashSet<String> result = cache.get(account);
        if (result != null) {
            return result;
        }
        result = new HashSet<>();
        cache.put(account, result);
        Cursor cursor = null;
        try {
            cursor = helper().getReadableDatabase().query(
                true,
                TABLE,
                new String[] { "dialog", "mid" },
                "account = ?",
                new String[] { String.valueOf(account) },
                null, null, null, null);
            while (cursor.moveToNext()) {
                result.add(key(cursor.getLong(0), cursor.getInt(1)));
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
                + "date INTEGER NOT NULL, "
                + "text TEXT NOT NULL)");
            database.execSQL("CREATE INDEX edits_message ON " + TABLE + " (account, dialog, mid)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        }
    }
}
