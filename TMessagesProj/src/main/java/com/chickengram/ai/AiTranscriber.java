package com.chickengram.ai;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;

import java.io.File;

public final class AiTranscriber {

    public interface Callback {
        void onResult(String text);
    }

    private AiTranscriber() {
    }

    public static void transcribe(MessageObject message, Callback callback) {
        Utilities.globalQueue.postRunnable(() -> {
            String text;
            try {
                text = AiClient.transcribe(file(message));
            } catch (Exception e) {
                text = "⚠ " + (e.getMessage() != null ? e.getMessage() : e.toString());
            }
            callback.onResult(text);
        });
    }

    private static File file(MessageObject message) throws Exception {
        final int account = message.currentAccount;
        File file = FileLoader.getInstance(account).getPathToMessage(message.messageOwner);
        if (file != null && file.exists() && file.length() > 0) {
            return file;
        }
        final TLRPC.Document document = message.getDocument();
        if (document == null) {
            throw new Exception("Не найден файл сообщения.");
        }
        AndroidUtilities.runOnUIThread(() -> FileLoader.getInstance(account).loadFile(document, message, FileLoader.PRIORITY_HIGH, 0));
        for (int i = 0; i < 180; i++) {
            Thread.sleep(500);
            file = FileLoader.getInstance(account).getPathToMessage(message.messageOwner);
            if (file != null && file.exists() && file.length() > 0) {
                return file;
            }
        }
        throw new Exception("Не удалось скачать сообщение для расшифровки.");
    }
}
