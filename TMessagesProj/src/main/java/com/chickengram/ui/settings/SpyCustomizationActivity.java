package com.chickengram.ui.settings;

import com.chickengram.ChickengramConfig;

import org.telegram.ui.Components.UItem;

import java.util.ArrayList;

import tw.nekomimi.nekogram.NekoConfig;

public class SpyCustomizationActivity extends BasePreferencesFragment {

    private ColorRowView colorRow;

    @Override
    protected CharSequence getTitle() {
        return "Кастомизация";
    }

    @Override
    protected void fill(ArrayList<UItem> items) {
        if (colorRow == null) {
            colorRow = new ColorRowView(getContext(), ChickengramConfig.DELETED_MARK_COLORS, ChickengramConfig.deletedMarkColorIndex(), index -> ChickengramConfig.setDeletedMarkColor(index));
        }
        items.add(UItem.asHeader("Удалённые сообщения"));
        items.add(check("Полупрозрачные удалённые", ChickengramConfig::semiTransparentDeleted, ChickengramConfig::setSemiTransparentDeleted));
        final int style = ChickengramConfig.deletedMarkStyle();
        items.add(radio("Метка — иконка корзины", style == ChickengramConfig.DELETED_MARK_ICON, v -> {
            ChickengramConfig.setDeletedMarkStyle(ChickengramConfig.DELETED_MARK_ICON);
            refresh();
        }));
        items.add(radio("Метка — слово «удалено»", style == ChickengramConfig.DELETED_MARK_TEXT, v -> {
            ChickengramConfig.setDeletedMarkStyle(ChickengramConfig.DELETED_MARK_TEXT);
            refresh();
        }));
        items.add(UItem.asHeader("Цвет метки"));
        items.add(UItem.asCustom(colorRow));
        items.add(UItem.asShadow("Цвет иконки у удалённых сообщений и плашки «удалено» на фото и видео в профиле. Изменения видны при следующем открытии чата."));

        items.add(UItem.asHeader("Полезные функции"));
        items.add(restartCheck("Локальный Telegram Premium", () -> NekoConfig.localPremium.Bool(), NekoConfig.localPremium::setConfigBool));
        items.add(check("Отключить рекламу", () -> NekoConfig.hideSponsoredMessage.Bool(), NekoConfig.hideSponsoredMessage::setConfigBool));
        items.add(UItem.asShadow("С локальным Premium вы не получите увеличение лимитов и не сможете отправлять анимированные эмодзи. Другие пользователи не увидят ваш премиум-статус."));
    }
}
