package com.chickengram;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;

public final class ChickengramConfig {

    private static final String PREFERENCES = "chickengram";
    private static final String KEEP_DELETED = "keepDeletedMessages";
    private static final String QUICK_PHRASE = "quickPhraseButton";

    private ChickengramConfig() {
    }

    public static boolean keepDeletedMessages() {
        return preferences().getBoolean(KEEP_DELETED, true);
    }

    public static void setKeepDeletedMessages(boolean value) {
        preferences().edit().putBoolean(KEEP_DELETED, value).apply();
    }

    public static boolean quickPhraseButton() {
        return preferences().getBoolean(QUICK_PHRASE, true);
    }

    public static void setQuickPhraseButton(boolean value) {
        preferences().edit().putBoolean(QUICK_PHRASE, value).apply();
    }

    private static SharedPreferences preferences() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
