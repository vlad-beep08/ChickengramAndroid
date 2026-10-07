package com.chickengram.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.chickengram.ChickengramConfig;

import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.DocumentSelectActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import tw.nekomimi.nekogram.helpers.AppRestartHelper;
import tw.nekomimi.nekogram.settings.NekoSettingsActivity;
import tw.nekomimi.nekogram.utils.EnvUtil;
import tw.nekomimi.nekogram.utils.FileUtil;
import tw.nekomimi.nekogram.utils.ShareUtil;

public class OtherPreferencesActivity extends BasePreferencesFragment {

    @Override
    protected CharSequence getTitle() {
        return "Другое";
    }

    @Override
    protected void fill(ArrayList<UItem> items) {
        items.add(UItem.asHeader("Настройки"));
        items.add(button(R.drawable.msg_settings, "Расширенные настройки", null, v -> presentFragment(new NekoSettingsActivity())));
        items.add(button(R.drawable.msg_shareout, "Экспортировать настройки", null, v -> exportSettings()));
        items.add(button(R.drawable.msg_download, "Импортировать настройки", null, v -> presentFragment(createImportPicker())));
        items.add(button(R.drawable.msg_reset, "Сбросить настройки Чикенграма", null, v -> confirm(
            "Сбросить настройки",
            "Все настройки Чикенграма вернутся к значениям по умолчанию. Сохранённые удалённые сообщения и история правок останутся.",
            "Сбросить",
            () -> {
                ChickengramConfig.reset();
                refresh();
                AppRestartHelper.triggerRebirth();
            })).red());
        items.add(UItem.asShadow("Экспорт сохраняет все настройки Чикенграма и Nagram в файл. Откройте этот файл в приложении, чтобы восстановить настройки."));
    }

    private void exportSettings() {
        try {
            final JSONObject json = new JSONObject(NekoSettingsActivity.backupSettingsJson(4));
            final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("chickengram", Context.MODE_PRIVATE);
            final JSONObject ours = new JSONObject();
            for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
                ours.put(entry.getKey(), entry.getValue());
            }
            json.put("chickengram", ours);
            final File file = new File(EnvUtil.getShareCachePath(), "chickengram-" + System.currentTimeMillis() + ".nekox-settings.json");
            FileUtil.writeUtf8String(json.toString(4), file);
            ShareUtil.shareFile(getParentActivity(), file);
        } catch (Exception e) {
            BulletinFactory.of(this).createSimpleBulletin(R.raw.error, "Не удалось экспортировать настройки").show();
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
                if (files == null || files.isEmpty() || getParentActivity() == null) {
                    return;
                }
                NekoSettingsActivity.importSettings(getParentActivity(), new File(files.get(0)));
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
}
