package com.chickengram.ui.settings;

import com.chickengram.ChickengramConfig;
import com.chickengram.messages.DeletedMessages;
import com.chickengram.messages.EditHistory;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.DocumentSelectActivity;

import java.io.File;
import java.util.ArrayList;

import tw.nekomimi.nekogram.utils.EnvUtil;
import tw.nekomimi.nekogram.utils.ShareUtil;

public class SpyStoragePreferencesActivity extends BasePreferencesFragment {

    @Override
    protected CharSequence getTitle() {
        return "Шпион";
    }

    @Override
    protected void fill(ArrayList<UItem> items) {
        items.add(UItem.asHeader("Режим шпиона"));
        items.add(check("Сохранять удалённые сообщения", ChickengramConfig::keepDeletedMessages, ChickengramConfig::setKeepDeletedMessages));
        items.add(check("Сохранять историю правок", ChickengramConfig::saveEditHistory, ChickengramConfig::setSaveEditHistory));
        items.add(UItem.asShadow("Удалённые сообщения остаются в чате и в профиле с пометкой. Старые версии изменённых сообщений можно посмотреть в меню сообщения → «История изменений». Сохранено удалённых: " + DeletedMessages.count() + ", правок: " + EditHistory.count() + "."));

        items.add(check("Сохранять последний онлайн", ChickengramConfig::saveLastOnline, ChickengramConfig::setSaveLastOnline));
        items.add(UItem.asShadow("Запоминает, когда человек со скрытым временем захода в последний раз писал или печатал. Вместо «был(а) недавно» покажется примерное время со знаком ≈."));

        items.add(UItem.asHeader("База данных"));
        items.add(button(R.drawable.msg_shareout, "Экспорт базы данных", null, v -> exportDatabase()));
        items.add(button(R.drawable.msg_download, "Импорт базы данных", null, v -> presentFragment(createImportPicker())));
        items.add(button(R.drawable.msg_delete, "Очистить", null, v -> confirm(
            "Очистить базу",
            "Сохранённые удалённые сообщения и история правок будут стёрты без возможности восстановления.",
            "Очистить",
            () -> {
                DeletedMessages.clear();
                EditHistory.clear();
                com.chickengram.messages.LastOnline.clear();
                refresh();
            })).red());
        items.add(UItem.asShadow("Экспорт сохраняет базу удалённых сообщений в файл, чтобы перенести её на другой телефон или сделать резервную копию."));
    }

    private void exportDatabase() {
        try {
            final File file = DeletedMessages.exportDatabase(EnvUtil.getShareCachePath());
            ShareUtil.shareFile(getParentActivity(), file);
        } catch (Exception e) {
            BulletinFactory.of(this).createSimpleBulletin(R.raw.error, "Не удалось экспортировать базу").show();
        }
    }

    private DocumentSelectActivity createImportPicker() {
        final DocumentSelectActivity picker = new DocumentSelectActivity(false);
        picker.setMaxSelectedFiles(1);
        picker.setAllowPhoto(false);
        picker.setDelegate(new DocumentSelectActivity.DocumentSelectActivityDelegate() {
            @Override
            public void didSelectFiles(DocumentSelectActivity activity, ArrayList<String> files, String caption, boolean notify, int scheduleDate) {
                activity.finishFragment();
                if (files == null || files.isEmpty()) {
                    return;
                }
                importDatabase(new File(files.get(0)));
            }

            @Override
            public void didSelectPhotos(ArrayList<SendMessagesHelper.SendingMediaInfo> photos, boolean notify, int scheduleDate) {
            }

            @Override
            public void startDocumentSelectActivity() {
            }
        });
        return picker;
    }

    private void importDatabase(File file) {
        final int imported = DeletedMessages.importDatabase(file);
        AndroidUtilities.runOnUIThread(() -> {
            if (getParentActivity() == null) {
                return;
            }
            final AlertDialog dialog = new AlertDialog(getParentActivity(), 0);
            dialog.setTitle("Импорт базы");
            dialog.setMessage(imported < 0
                ? "Этот файл не похож на базу удалённых сообщений Чикенграма."
                : "Добавлено записей: " + imported + ".");
            dialog.setPositiveButton("OK", null);
            showDialog(dialog);
            refresh();
        });
    }
}
