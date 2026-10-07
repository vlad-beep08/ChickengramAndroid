package com.chickengram.ui.settings;

import com.chickengram.ChickengramConfig;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;

import java.util.ArrayList;

import tw.nekomimi.nekogram.NekoConfig;
import tw.nekomimi.nekogram.settings.NekoGeneralSettingsActivity;
import xyz.nextalone.nagram.NaConfig;

public class GeneralPreferencesActivity extends BasePreferencesFragment {

    @Override
    protected CharSequence getTitle() {
        return "Основные";
    }

    @Override
    protected void fill(ArrayList<UItem> items) {
        items.add(UItem.asHeader("Перевод сообщений"));
        items.add(check("Показывать кнопку «Перевести»", () -> NekoConfig.showTranslate.Bool(), NekoConfig.showTranslate::setConfigBool));
        items.add(check("Переводить чаты целиком", () -> NaConfig.INSTANCE.getAutoTranslate().Bool(), v -> NaConfig.INSTANCE.getAutoTranslate().setConfigBool(v)));
        items.add(button(R.drawable.msg_translate, "Сервис и язык перевода", null, v -> presentFragment(new NekoGeneralSettingsActivity())));
        items.add(UItem.asShadow("Кнопка «Перевести» появится в меню по нажатию на текстовое сообщение."));

        items.add(UItem.asHeader("Основные"));
        items.add(check("Отключить округление чисел", () -> NekoConfig.disableNumberRounding.Bool(), NekoConfig.disableNumberRounding::setConfigBool));
        items.add(check("Форматировать время с секундами", () -> NekoConfig.showSeconds.Bool(), v -> {
            NekoConfig.showSeconds.setConfigBool(v);
            LocaleController.getInstance().recreateFormatters();
        }));
        items.add(check("Вибрация в приложении", () -> !NekoConfig.disableVibration.Bool(), v -> NekoConfig.disableVibration.setConfigBool(!v)));
        items.add(check("Фильтр «Zalgo»", () -> NaConfig.INSTANCE.getZalgoFilter().Bool(), v -> NaConfig.INSTANCE.getZalgoFilter().setConfigBool(v)));
        items.add(UItem.asShadow("Округление: 1.23K → 1 234. Секунды: 12:34 → 12:34:56. Фильтр «Zalgo» убирает искажающие текст символы в именах и сообщениях."));

        items.add(UItem.asHeader("Ускорение загрузки"));
        int boost = ChickengramConfig.downloadBoost();
        if (boost == 0 && NekoConfig.enhancedFileLoader.Bool()) {
            boost = 1;
        }
        items.add(UItem.asSlideView(new String[] { "Откл.", "Быстро", "Ультра" }, boost, index -> {
            ChickengramConfig.setDownloadBoost(index);
            NekoConfig.enhancedFileLoader.setConfigBool(index > 0);
        }));
        items.add(check("Ускорение отправки", ChickengramConfig::uploadBoost, ChickengramConfig::setUploadBoost));
        items.add(UItem.asShadow("Ультра-ускорение может вызывать проблемы с загрузкой файлов и просмотром видео на медленном интернет-соединении."));

        items.add(UItem.asHeader("Профиль"));
        items.add(check("Относительное время онлайна", ChickengramConfig::relativeOnline, ChickengramConfig::setRelativeOnline));
        items.add(check("Скрыть номер телефона", () -> NekoConfig.hidePhone.Bool(), NekoConfig.hidePhone::setConfigBool));
        items.add(check("Показывать ID и DC", () -> NekoConfig.showIdAndDc.Bool(), NekoConfig.showIdAndDc::setConfigBool));
        items.add(UItem.asShadow("Относительное время: «был(а) 5 мин. назад» вместо точного времени. Telegram API показывает ID как есть, а Bot API добавляет минус для групп и −100 для супергрупп и каналов."));

        items.add(UItem.asHeader("Архив чатов"));
        items.add(check("Открывать архив при вытягивании", () -> NekoConfig.openArchiveOnPull.Bool(), NekoConfig.openArchiveOnPull::setConfigBool));
        items.add(check("Отключить жест «Вернуть»", () -> NaConfig.INSTANCE.getDoNotUnarchiveBySwipe().Bool(), v -> NaConfig.INSTANCE.getDoNotUnarchiveBySwipe().setConfigBool(v)));
        items.add(UItem.asShadow("Свайп влево не будет убирать чаты из архива."));
    }
}
