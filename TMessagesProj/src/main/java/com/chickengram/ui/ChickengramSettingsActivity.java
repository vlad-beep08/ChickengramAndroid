package com.chickengram.ui;

import android.view.View;

import com.chickengram.ChickengramConfig;
import com.chickengram.messages.DeletedMessages;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.DocumentSelectActivity;

import java.io.File;
import java.util.ArrayList;

import tw.nekomimi.nekogram.utils.EnvUtil;
import tw.nekomimi.nekogram.utils.ShareUtil;

public class ChickengramSettingsActivity extends UniversalFragment {

    private static final int KEEP_DELETED = 1;
    private static final int QUICK_PHRASE = 2;
    private static final int EXPORT = 3;
    private static final int IMPORT = 4;

    @Override
    protected CharSequence getTitle() {
        return "Чикенграм";
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader("Удалённые сообщения"));
        items.add(UItem.asCheck(KEEP_DELETED, "Сохранять удалённые сообщения").setChecked(ChickengramConfig.keepDeletedMessages()));
        items.add(UItem.asShadow("Сообщения, которые удалил собеседник или админ, остаются в чате и в профиле с пометкой «удалено». Сохранено: " + DeletedMessages.count() + "."));
        items.add(UItem.asHeader("База удалённых сообщений"));
        items.add(UItem.asButton(EXPORT, R.drawable.msg_shareout, "Экспортировать базу"));
        items.add(UItem.asButton(IMPORT, R.drawable.msg_download, "Импортировать базу"));
        items.add(UItem.asShadow("Экспорт сохраняет базу в файл, чтобы перенести её на другой телефон или сделать резервную копию."));
        items.add(UItem.asHeader("Кнопка ЖОПА"));
        items.add(UItem.asCheck(QUICK_PHRASE, "Показывать кнопку ЖОПА").setChecked(ChickengramConfig.quickPhraseButton()));
        items.add(UItem.asShadow("Кнопка у поля ввода отправляет «жопа» одним нажатием. Изменение видно при следующем открытии чата."));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == KEEP_DELETED) {
            ChickengramConfig.setKeepDeletedMessages(!ChickengramConfig.keepDeletedMessages());
            listView.adapter.update(true);
        } else if (item.id == QUICK_PHRASE) {
            ChickengramConfig.setQuickPhraseButton(!ChickengramConfig.quickPhraseButton());
            listView.adapter.update(true);
        } else if (item.id == EXPORT) {
            exportDatabase();
        } else if (item.id == IMPORT) {
            presentFragment(createImportPicker());
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
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
            listView.adapter.update(true);
        });
    }
}
