package com.chickengram.messages;

import android.content.Context;
import android.content.SharedPreferences;

import com.chickengram.ChickengramConfig;

import org.telegram.messenger.ApplicationLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LastOnline {

    private static final String PREFERENCES = "chickengram_last_online";
    private static final ConcurrentHashMap<Long, Integer> seen = new ConcurrentHashMap<>();
    private static volatile boolean loaded;

    private LastOnline() {
    }

    public static void remember(long userId, int date) {
        if (userId <= 0 || date <= 0 || !ChickengramConfig.saveLastOnline()) {
            return;
        }
        load();
        final Integer old = seen.get(userId);
        if (old != null && old >= date) {
            return;
        }
        seen.put(userId, date);
        preferences().edit().putInt(String.valueOf(userId), date).apply();
    }

    public static int get(long userId) {
        if (!ChickengramConfig.saveLastOnline()) {
            return 0;
        }
        load();
        final Integer value = seen.get(userId);
        return value == null ? 0 : value;
    }

    public static void clear() {
        seen.clear();
        preferences().edit().clear().apply();
    }

    private static void load() {
        if (loaded) {
            return;
        }
        synchronized (LastOnline.class) {
            if (loaded) {
                return;
            }
            for (Map.Entry<String, ?> entry : preferences().getAll().entrySet()) {
                if (entry.getValue() instanceof Integer) {
                    try {
                        seen.put(Long.parseLong(entry.getKey()), (Integer) entry.getValue());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            loaded = true;
        }
    }

    private static SharedPreferences preferences() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
