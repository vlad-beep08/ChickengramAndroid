package com.chickengram.messages;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.collection.LongSparseArray;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class DeletedMessages {

    public static final String LABEL = "удалено";

    private static final String DATABASE = "chickengram_deleted.db";
    private static final String TABLE = "deleted";
    private static final int VERSION = 1;
    private static final String PREFERENCES = "chickengram";
    private static final String KEEP_KEY = "keepDeletedMessages";

    private static final Object lock = new Object();
    private static final HashMap<Integer, HashMap<Long, HashSet<Integer>>> cache = new HashMap<>();
    private static Helper helper;

    private DeletedMessages() {
    }

    public static boolean keepDeleted() {
        return preferences().getBoolean(KEEP_KEY, true);
    }

    public static void setKeepDeleted(boolean value) {
        preferences().edit().putBoolean(KEEP_KEY, value).apply();
    }

    public static void remember(int account, LongSparseArray<ArrayList<Integer>> deleted) {
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
                        values.put("date", date);
                        database.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                    }
                }
                database.setTransactionSuccessful();
            } catch (Exception e) {
                FileLog.e(e);
            } finally {
                if (database != null) {
                    try {
                        database.endTransaction();
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
            }
        }
        for (int i = 0; i < deleted.size(); i++) {
            final long dialog = deleted.keyAt(i);
            final ArrayList<Integer> ids = deleted.valueAt(i);
            if (ids == null) {
                continue;
            }
            AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(account)
                .postNotificationName(NotificationCenter.chickengramMessagesDeleted, dialog, ids));
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

    private static Helper helper() {
        if (helper == null) {
            helper = new Helper(ApplicationLoader.applicationContext);
        }
        return helper;
    }

    private static SharedPreferences preferences() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
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
                + "PRIMARY KEY (account, dialog, mid))");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        }
    }
}
