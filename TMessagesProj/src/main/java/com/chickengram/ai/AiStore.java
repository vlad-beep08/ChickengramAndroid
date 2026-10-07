package com.chickengram.ai;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

import java.util.ArrayList;

public final class AiStore {

    public static final class Role {
        public String name;
        public String prompt;

        public Role(String name, String prompt) {
            this.name = name;
            this.prompt = prompt;
        }
    }

    public static final class Chat {
        public final long id;
        public final String title;
        public final String role;
        public final int updated;

        Chat(long id, String title, String role, int updated) {
            this.id = id;
            this.title = title;
            this.role = role;
            this.updated = updated;
        }
    }

    private static final String PREFERENCES = "chickengram_ai";
    private static final String DATABASE = "chickengram_ai.db";
    private static final Object lock = new Object();
    private static Helper helper;

    private AiStore() {
    }

    public static ArrayList<Role> defaultRoles() {
        final ArrayList<Role> roles = new ArrayList<>();
        roles.add(new Role("Помощник", "Ты — дружелюбный и толковый помощник внутри мессенджера Чикенграм. Отвечай на языке собеседника, по делу и без лишней воды."));
        roles.add(new Role("Переводчик", "Ты — профессиональный переводчик. Переводи присланный текст: с русского на английский, с любого другого языка — на русский. Отвечай только переводом."));
        roles.add(new Role("Редактор", "Ты — литературный редактор. Исправляй ошибки, улучшай стиль и сохраняй смысл. Отвечай только исправленным текстом."));
        roles.add(new Role("Программист", "Ты — опытный программист. Объясняй понятно, приводи рабочие примеры кода и предупреждай о подводных камнях."));
        roles.add(new Role("Шутник", "Ты — весёлый собеседник с хорошим чувством юмора. Отвечай остроумно и коротко, но без грубости."));
        return roles;
    }

    public static ArrayList<Role> roles() {
        final String json = preferences().getString("roles", null);
        if (json != null) {
            try {
                final JSONArray array = new JSONArray(json);
                final ArrayList<Role> roles = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    final JSONObject object = array.getJSONObject(i);
                    roles.add(new Role(object.optString("name"), object.optString("prompt")));
                }
                if (!roles.isEmpty()) {
                    return roles;
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        return defaultRoles();
    }

    public static void saveRoles(ArrayList<Role> roles) {
        final JSONArray array = new JSONArray();
        try {
            for (Role role : roles) {
                array.put(new JSONObject().put("name", role.name).put("prompt", role.prompt));
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        preferences().edit().putString("roles", array.toString()).apply();
    }

    public static int selectedRoleIndex() {
        final int index = preferences().getInt("role", 0);
        final int size = roles().size();
        return index >= 0 && index < size ? index : 0;
    }

    public static Role selectedRole() {
        return roles().get(selectedRoleIndex());
    }

    public static void setSelectedRole(int index) {
        preferences().edit().putInt("role", index).apply();
    }

    public static boolean saveHistory() {
        return preferences().getBoolean("saveHistory", true);
    }

    public static void setSaveHistory(boolean value) {
        preferences().edit().putBoolean("saveHistory", value).apply();
    }

    public static long createChat(String title, String role) {
        synchronized (lock) {
            try {
                final ContentValues values = new ContentValues();
                values.put("title", title);
                values.put("role", role);
                values.put("updated", (int) (System.currentTimeMillis() / 1000));
                return helper().getWritableDatabase().insert("chats", null, values);
            } catch (Exception e) {
                FileLog.e(e);
                return -1;
            }
        }
    }

    public static void addMessage(long chat, String role, String text) {
        if (chat < 0) {
            return;
        }
        synchronized (lock) {
            try {
                final int now = (int) (System.currentTimeMillis() / 1000);
                final ContentValues values = new ContentValues();
                values.put("chat", chat);
                values.put("role", role);
                values.put("text", text);
                values.put("date", now);
                final SQLiteDatabase database = helper().getWritableDatabase();
                database.insert("messages", null, values);
                final ContentValues update = new ContentValues();
                update.put("updated", now);
                database.update("chats", update, "id = ?", new String[] { String.valueOf(chat) });
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public static ArrayList<Chat> chats() {
        final ArrayList<Chat> result = new ArrayList<>();
        synchronized (lock) {
            Cursor cursor = null;
            try {
                cursor = helper().getReadableDatabase().query("chats", new String[] { "id", "title", "role", "updated" }, null, null, null, null, "updated DESC");
                while (cursor.moveToNext()) {
                    result.add(new Chat(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3)));
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

    public static Chat chat(long id) {
        for (Chat chat : chats()) {
            if (chat.id == id) {
                return chat;
            }
        }
        return null;
    }

    public static ArrayList<AiClient.Message> messages(long chat) {
        final ArrayList<AiClient.Message> result = new ArrayList<>();
        synchronized (lock) {
            Cursor cursor = null;
            try {
                cursor = helper().getReadableDatabase().query("messages", new String[] { "role", "text" }, "chat = ?", new String[] { String.valueOf(chat) }, null, null, "id ASC");
                while (cursor.moveToNext()) {
                    result.add(new AiClient.Message(cursor.getString(0), cursor.getString(1)));
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

    public static void deleteChat(long id) {
        synchronized (lock) {
            try {
                final SQLiteDatabase database = helper().getWritableDatabase();
                database.delete("messages", "chat = ?", new String[] { String.valueOf(id) });
                database.delete("chats", "id = ?", new String[] { String.valueOf(id) });
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public static void clearHistory() {
        synchronized (lock) {
            try {
                final SQLiteDatabase database = helper().getWritableDatabase();
                database.delete("messages", null, null);
                database.delete("chats", null, null);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    private static SharedPreferences preferences() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    private static Helper helper() {
        if (helper == null) {
            helper = new Helper(ApplicationLoader.applicationContext);
        }
        return helper;
    }

    private static final class Helper extends SQLiteOpenHelper {

        Helper(Context context) {
            super(context, DATABASE, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL("CREATE TABLE chats (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, role TEXT, updated INTEGER NOT NULL)");
            database.execSQL("CREATE TABLE messages (id INTEGER PRIMARY KEY AUTOINCREMENT, chat INTEGER NOT NULL, role TEXT NOT NULL, text TEXT NOT NULL, date INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX messages_chat ON messages (chat)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        }
    }
}
