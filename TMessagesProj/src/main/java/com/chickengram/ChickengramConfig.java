package com.chickengram;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;

public final class ChickengramConfig {

    public static final int MAX_AVATAR_CORNERS = 28;

    public static final int STICKER_SHAPE_DEFAULT = 0;
    public static final int STICKER_SHAPE_ROUNDED = 1;
    public static final int STICKER_SHAPE_MESSAGE = 2;

    public static final int SWIPE_REPLY = 0;
    public static final int SWIPE_REPEAT = 1;
    public static final int SWIPE_SAVE = 2;
    public static final int SWIPE_FORWARD = 3;
    public static final int SWIPE_TRANSLATE = 4;
    public static final int SWIPE_COPY = 5;
    public static final int SWIPE_DISABLED = 6;

    public static final int DELETED_MARK_ICON = 0;
    public static final int DELETED_MARK_TEXT = 1;

    public static final int[] DELETED_MARK_COLORS = {
        0xFF8E8E93, 0xFFFF3B30, 0xFFE53935, 0xFFE91E63, 0xFFD81BDE, 0xFF9C27B0, 0xFF673AB7, 0xFF3D5AFE
    };

    private static final String PREFERENCES = "chickengram";

    private static volatile boolean loaded;
    private static boolean keepDeletedMessages;
    private static boolean saveEditHistory;
    private static boolean quickPhraseButton;
    private static int avatarCorners;
    private static boolean solarIcons;
    private static boolean squareFab;
    private static boolean disableDividers;
    private static int stickerShape;
    private static boolean hideShareButton;
    private static boolean removeMessageTail;
    private static int doubleTapSeek;
    private static boolean staticZoom;
    private static boolean alwaysHd;
    private static boolean calcResults;
    private static boolean typingNuts;
    private static boolean relativeOnline;
    private static int downloadBoost;
    private static boolean uploadBoost;
    private static boolean volumeUnmute;
    private static boolean semiTransparentDeleted;
    private static int deletedMarkStyle;
    private static int deletedMarkColor;
    private static int swipeAction;
    private static boolean silentSend;
    private static boolean saveLastOnline;
    private static boolean ghostSchedule;
    private static boolean ghostTitle;
    private static boolean replyColors;
    private static boolean replyEmoji;
    private static boolean replyBackground;
    private static boolean suggestGhostStories;
    private static boolean aiTranscribe;
    private static int sectionRadius;
    private static boolean separateHeaders;
    private static boolean widePosts;
    private static boolean pillGhost;
    private static boolean pillSaved;
    private static boolean pillArchive;
    private static boolean pillAi;

    private ChickengramConfig() {
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        synchronized (ChickengramConfig.class) {
            if (loaded) {
                return;
            }
            final SharedPreferences p = preferences();
            keepDeletedMessages = p.getBoolean("keepDeletedMessages", true);
            saveEditHistory = p.getBoolean("saveEditHistory", true);
            quickPhraseButton = p.getBoolean("quickPhraseButton", true);
            avatarCorners = p.getInt("avatarCorners", MAX_AVATAR_CORNERS);
            solarIcons = p.getBoolean("solarIcons", false);
            squareFab = p.getBoolean("squareFab", false);
            disableDividers = p.getBoolean("disableDividers", false);
            stickerShape = p.getInt("stickerShape", STICKER_SHAPE_DEFAULT);
            hideShareButton = p.getBoolean("hideShareButton", false);
            removeMessageTail = p.getBoolean("removeMessageTail", false);
            doubleTapSeek = p.getInt("doubleTapSeek", 10);
            staticZoom = p.getBoolean("staticZoom", false);
            alwaysHd = p.getBoolean("alwaysHd", false);
            calcResults = p.getBoolean("calcResults", false);
        typingNuts = p.getBoolean("typingNuts", true);
            relativeOnline = p.getBoolean("relativeOnline", false);
            downloadBoost = p.getInt("downloadBoost", 0);
            uploadBoost = p.getBoolean("uploadBoost", false);
            volumeUnmute = p.getBoolean("volumeUnmute", true);
            semiTransparentDeleted = p.getBoolean("semiTransparentDeleted", false);
            deletedMarkStyle = p.getInt("deletedMarkStyle", DELETED_MARK_ICON);
            deletedMarkColor = p.getInt("deletedMarkColor", 1);
            swipeAction = p.getInt("swipeAction", SWIPE_REPLY);
            silentSend = p.getBoolean("silentSend", false);
            saveLastOnline = p.getBoolean("saveLastOnline", true);
            ghostSchedule = p.getBoolean("ghostSchedule", false);
            ghostTitle = p.getBoolean("ghostTitle", false);
            replyColors = p.getBoolean("replyColors", true);
            replyEmoji = p.getBoolean("replyEmoji", true);
            replyBackground = p.getBoolean("replyBackground", true);
            suggestGhostStories = p.getBoolean("suggestGhostStories", true);
            aiTranscribe = p.getBoolean("aiTranscribe", false);
            sectionRadius = p.getInt("sectionRadius", -1);
            separateHeaders = p.getBoolean("separateHeaders", false);
            widePosts = p.getBoolean("widePosts", false);
            pillGhost = p.getBoolean("pillGhost", true);
            pillSaved = p.getBoolean("pillSaved", true);
            pillArchive = p.getBoolean("pillArchive", false);
            pillAi = p.getBoolean("pillAi", false);
            loaded = true;
        }
    }

    public static boolean keepDeletedMessages() {
        ensureLoaded();
        return keepDeletedMessages;
    }

    public static void setKeepDeletedMessages(boolean value) {
        keepDeletedMessages = put("keepDeletedMessages", value);
    }

    public static boolean saveEditHistory() {
        ensureLoaded();
        return saveEditHistory;
    }

    public static void setSaveEditHistory(boolean value) {
        saveEditHistory = put("saveEditHistory", value);
    }

    public static boolean quickPhraseButton() {
        ensureLoaded();
        return quickPhraseButton;
    }

    public static void setQuickPhraseButton(boolean value) {
        quickPhraseButton = put("quickPhraseButton", value);
    }

    public static int avatarCorners() {
        ensureLoaded();
        return avatarCorners;
    }

    public static void setAvatarCorners(int value) {
        avatarCorners = put("avatarCorners", Math.max(0, Math.min(MAX_AVATAR_CORNERS, value)));
    }

    public static boolean solarIcons() {
        ensureLoaded();
        return solarIcons;
    }

    public static void setSolarIcons(boolean value) {
        solarIcons = put("solarIcons", value);
    }

    public static boolean squareFab() {
        ensureLoaded();
        return squareFab;
    }

    public static void setSquareFab(boolean value) {
        squareFab = put("squareFab", value);
    }

    public static boolean disableDividers() {
        ensureLoaded();
        return disableDividers;
    }

    public static void setDisableDividers(boolean value) {
        disableDividers = put("disableDividers", value);
    }

    public static int stickerShape() {
        ensureLoaded();
        return stickerShape;
    }

    public static void setStickerShape(int value) {
        stickerShape = put("stickerShape", value);
    }

    public static boolean hideShareButton() {
        ensureLoaded();
        return hideShareButton;
    }

    public static void setHideShareButton(boolean value) {
        hideShareButton = put("hideShareButton", value);
    }

    public static boolean removeMessageTail() {
        ensureLoaded();
        return removeMessageTail;
    }

    public static void setRemoveMessageTail(boolean value) {
        removeMessageTail = put("removeMessageTail", value);
    }

    public static int doubleTapSeek() {
        ensureLoaded();
        return doubleTapSeek;
    }

    public static void setDoubleTapSeek(int value) {
        doubleTapSeek = put("doubleTapSeek", value);
    }

    public static boolean staticZoom() {
        ensureLoaded();
        return staticZoom;
    }

    public static void setStaticZoom(boolean value) {
        staticZoom = put("staticZoom", value);
    }

    public static boolean alwaysHd() {
        ensureLoaded();
        return alwaysHd;
    }

    public static void setAlwaysHd(boolean value) {
        alwaysHd = put("alwaysHd", value);
    }

    public static boolean calcResults() {
        ensureLoaded();
        return calcResults;
    }

    public static void setCalcResults(boolean value) {
        calcResults = put("calcResults", value);
    }

    public static boolean typingNuts() {
        ensureLoaded();
        return typingNuts;
    }

    public static void setTypingNuts(boolean value) {
        typingNuts = put("typingNuts", value);
    }

    public static boolean relativeOnline() {
        ensureLoaded();
        return relativeOnline;
    }

    public static void setRelativeOnline(boolean value) {
        relativeOnline = put("relativeOnline", value);
    }

    public static int downloadBoost() {
        ensureLoaded();
        return downloadBoost;
    }

    public static void setDownloadBoost(int value) {
        downloadBoost = put("downloadBoost", value);
    }

    public static boolean uploadBoost() {
        ensureLoaded();
        return uploadBoost;
    }

    public static void setUploadBoost(boolean value) {
        uploadBoost = put("uploadBoost", value);
    }

    public static boolean volumeUnmute() {
        ensureLoaded();
        return volumeUnmute;
    }

    public static void setVolumeUnmute(boolean value) {
        volumeUnmute = put("volumeUnmute", value);
    }

    public static boolean semiTransparentDeleted() {
        ensureLoaded();
        return semiTransparentDeleted;
    }

    public static void setSemiTransparentDeleted(boolean value) {
        semiTransparentDeleted = put("semiTransparentDeleted", value);
    }

    public static int deletedMarkStyle() {
        ensureLoaded();
        return deletedMarkStyle;
    }

    public static void setDeletedMarkStyle(int value) {
        deletedMarkStyle = put("deletedMarkStyle", value);
    }

    public static int deletedMarkColorIndex() {
        ensureLoaded();
        return deletedMarkColor;
    }

    public static int deletedMarkColor() {
        ensureLoaded();
        return DELETED_MARK_COLORS[Math.max(0, Math.min(DELETED_MARK_COLORS.length - 1, deletedMarkColor))];
    }

    public static void setDeletedMarkColor(int index) {
        deletedMarkColor = put("deletedMarkColor", index);
    }

    public static int swipeAction() {
        ensureLoaded();
        return swipeAction;
    }

    public static void setSwipeAction(int value) {
        swipeAction = put("swipeAction", value);
    }

    public static boolean silentSend() {
        ensureLoaded();
        return silentSend;
    }

    public static void setSilentSend(boolean value) {
        silentSend = put("silentSend", value);
    }

    public static boolean saveLastOnline() {
        ensureLoaded();
        return saveLastOnline;
    }

    public static void setSaveLastOnline(boolean value) {
        saveLastOnline = put("saveLastOnline", value);
    }

    public static boolean ghostSchedule() {
        ensureLoaded();
        return ghostSchedule;
    }

    public static void setGhostSchedule(boolean value) {
        ghostSchedule = put("ghostSchedule", value);
    }

    public static boolean ghostTitle() {
        ensureLoaded();
        return ghostTitle;
    }

    public static void setGhostTitle(boolean value) {
        ghostTitle = put("ghostTitle", value);
    }

    public static boolean replyColors() {
        ensureLoaded();
        return replyColors;
    }

    public static void setReplyColors(boolean value) {
        replyColors = put("replyColors", value);
    }

    public static boolean replyEmoji() {
        ensureLoaded();
        return replyEmoji;
    }

    public static void setReplyEmoji(boolean value) {
        replyEmoji = put("replyEmoji", value);
    }

    public static boolean replyBackground() {
        ensureLoaded();
        return replyBackground;
    }

    public static void setReplyBackground(boolean value) {
        replyBackground = put("replyBackground", value);
    }

    public static boolean suggestGhostStories() {
        ensureLoaded();
        return suggestGhostStories;
    }

    public static void setSuggestGhostStories(boolean value) {
        suggestGhostStories = put("suggestGhostStories", value);
    }

    public static boolean aiTranscribe() {
        ensureLoaded();
        return aiTranscribe;
    }

    public static void setAiTranscribe(boolean value) {
        aiTranscribe = put("aiTranscribe", value);
    }

    public static int sectionRadius() {
        ensureLoaded();
        return sectionRadius;
    }

    public static void setSectionRadius(int value) {
        sectionRadius = put("sectionRadius", value);
    }

    public static boolean separateHeaders() {
        ensureLoaded();
        return separateHeaders;
    }

    public static void setSeparateHeaders(boolean value) {
        separateHeaders = put("separateHeaders", value);
    }

    public static boolean widePosts() {
        ensureLoaded();
        return widePosts;
    }

    public static void setWidePosts(boolean value) {
        widePosts = put("widePosts", value);
    }

    public static boolean pillGhost() {
        ensureLoaded();
        return pillGhost;
    }

    public static void setPillGhost(boolean value) {
        pillGhost = put("pillGhost", value);
    }

    public static boolean pillSaved() {
        ensureLoaded();
        return pillSaved;
    }

    public static void setPillSaved(boolean value) {
        pillSaved = put("pillSaved", value);
    }

    public static boolean pillArchive() {
        ensureLoaded();
        return pillArchive;
    }

    public static void setPillArchive(boolean value) {
        pillArchive = put("pillArchive", value);
    }

    public static boolean pillAi() {
        ensureLoaded();
        return pillAi;
    }

    public static void setPillAi(boolean value) {
        pillAi = put("pillAi", value);
    }

    public static void reset() {
        preferences().edit().clear().commit();
        loaded = false;
        ensureLoaded();
    }

    private static boolean put(String key, boolean value) {
        ensureLoaded();
        preferences().edit().putBoolean(key, value).apply();
        return value;
    }

    private static int put(String key, int value) {
        ensureLoaded();
        preferences().edit().putInt(key, value).apply();
        return value;
    }

    private static SharedPreferences preferences() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
