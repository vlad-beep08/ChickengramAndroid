package com.chickengram.ui.settings;

import com.chickengram.ChickengramConfig;

import org.telegram.ui.Components.UItem;

import java.util.ArrayList;

import tw.nekomimi.nekogram.NekoConfig;
import xyz.nextalone.nagram.NaConfig;
import xyz.nextalone.nagram.helper.DoubleTap;

public class ChatsPreferencesActivity extends BasePreferencesFragment {

    private static final String[] DOUBLE_TAP_TITLES = { "Отключить", "Реакции", "Выбор реакции", "Перевести", "Ответить", "Сохранить", "Повторить", "Повторить копией", "Изменить" };
    private static final int[] DOUBLE_TAP_VALUES = {
        DoubleTap.DOUBLE_TAP_ACTION_NONE,
        DoubleTap.DOUBLE_TAP_ACTION_SEND_REACTIONS,
        DoubleTap.DOUBLE_TAP_ACTION_SHOW_REACTIONS,
        DoubleTap.DOUBLE_TAP_ACTION_TRANSLATE,
        DoubleTap.DOUBLE_TAP_ACTION_REPLY,
        DoubleTap.DOUBLE_TAP_ACTION_SAVE,
        DoubleTap.DOUBLE_TAP_ACTION_REPEAT,
        DoubleTap.DOUBLE_TAP_ACTION_REPEAT_AS_COPY,
        DoubleTap.DOUBLE_TAP_ACTION_EDIT
    };
    private static final String[] SEEK_TITLES = { "5 сек.", "10 сек.", "15 сек.", "30 сек." };
    private static final int[] SEEK_VALUES = { 5, 10, 15, 30 };

    @Override
    protected CharSequence getTitle() {
        return "Чаты";
    }

    @Override
    protected void fill(ArrayList<UItem> items) {
        items.add(UItem.asHeader("Размер стикеров"));
        items.add(UItem.asIntSlideView(1, 4, Math.round(NekoConfig.stickerSize.Float()), 20,
            value -> value == 14 ? "По умолчанию" : String.valueOf(value),
            value -> NekoConfig.stickerSize.setConfigFloat(value.floatValue())));
        items.add(check("Скрыть время на стикерах", () -> NekoConfig.hideTimeForSticker.Bool(), NekoConfig.hideTimeForSticker::setConfigBool));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("Форма стикеров"));
        final int shape = ChickengramConfig.stickerShape();
        items.add(radio("По умолчанию", shape == ChickengramConfig.STICKER_SHAPE_DEFAULT, v -> setStickerShape(ChickengramConfig.STICKER_SHAPE_DEFAULT)));
        items.add(radio("Закруглённая", shape == ChickengramConfig.STICKER_SHAPE_ROUNDED, v -> setStickerShape(ChickengramConfig.STICKER_SHAPE_ROUNDED)));
        items.add(radio("Как сообщение", shape == ChickengramConfig.STICKER_SHAPE_MESSAGE, v -> setStickerShape(ChickengramConfig.STICKER_SHAPE_MESSAGE)));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("Стикеры и эмодзи"));
        items.add(check("Бесконечные недавние стикеры", () -> NekoConfig.maxRecentStickerCount.Int() > 20, v -> NekoConfig.maxRecentStickerCount.setConfigInt(v ? 200 : 20)));
        items.add(check("Скрыть реакции", () -> !NaConfig.INSTANCE.getShowReactions().Bool(), v -> NaConfig.INSTANCE.getShowReactions().setConfigBool(!v)));
        items.add(UItem.asShadow("Реакции будут скрыты из интерфейса."));

        items.add(UItem.asHeader("Жесты"));
        items.add(button(0, "Входящие сообщения", doubleTapTitle(NaConfig.INSTANCE.getDoubleTapActionIncoming().Int()), v -> chooseDoubleTap(false)));
        items.add(button(0, "Исходящие сообщения", doubleTapTitle(NaConfig.INSTANCE.getDoubleTapActionOutgoing().Int()), v -> chooseDoubleTap(true)));
        items.add(UItem.asShadow("Действие по двойному нажатию на сообщение. Некоторые действия требуют прав администратора в чате или канале."));

        items.add(UItem.asHeader("Чаты"));
        items.add(check("Кнопка ЖОПА", ChickengramConfig::quickPhraseButton, ChickengramConfig::setQuickPhraseButton));
        items.add(check("Скрыть кнопку «Звук» в каналах", () -> NaConfig.INSTANCE.getDisableChannelMuteButton().Bool(), v -> NaConfig.INSTANCE.getDisableChannelMuteButton().setConfigBool(v)));
        items.add(check("Быстрые админ-действия", () -> NekoConfig.showAdminActions.Bool(), NekoConfig.showAdminActions::setConfigBool));
        items.add(check("Быстрый переход свайпом", () -> !NekoConfig.disableSwipeToNext.Bool(), v -> NekoConfig.disableSwipeToNext.setConfigBool(!v)));
        items.add(check("Скрыть приветственный стикер", () -> NekoConfig.dontSendGreetingSticker.Bool(), NekoConfig.dontSendGreetingSticker::setConfigBool));
        items.add(check("Скрывать клавиатуру при прокрутке", () -> NekoConfig.hideKeyboardOnChatScroll.Bool(), NekoConfig.hideKeyboardOnChatScroll::setConfigBool));
        items.add(check("Запятая после упоминания", () -> NaConfig.INSTANCE.getAddCommaAfterMention().Bool(), v -> NaConfig.INSTANCE.getAddCommaAfterMention().setConfigBool(v)));
        items.add(check("Результаты вычислений", ChickengramConfig::calcResults, ChickengramConfig::setCalcResults));
        items.add(check("Скрыть кнопку «Отправить как»", () -> NekoConfig.hideSendAsChannel.Bool(), NekoConfig.hideSendAsChannel::setConfigBool));
        items.add(UItem.asShadow("Результаты вычислений: напишите «2+2=» и пробел — результат подставится сам. Кнопка ЖОПА появится у поля ввода при следующем открытии чата."));

        items.add(UItem.asHeader("Сообщения"));
        items.add(check("Убрать хвост у сообщений", ChickengramConfig::removeMessageTail, ChickengramConfig::setRemoveMessageTail));
        items.add(check("Заменять «изменено» иконкой", () -> NaConfig.INSTANCE.getShowEditedIcon().Bool(), v -> NaConfig.INSTANCE.getShowEditedIcon().setConfigBool(v)));
        items.add(check("Показывать индикатор онлайна", () -> NaConfig.INSTANCE.getShowOnlineStatus().Bool(), v -> NaConfig.INSTANCE.getShowOnlineStatus().setConfigBool(v)));
        items.add(check("Показывать число пересылок", () -> NaConfig.INSTANCE.getShowForwardCount().Bool(), v -> NaConfig.INSTANCE.getShowForwardCount().setConfigBool(v)));
        items.add(check("Скрыть боковую кнопку «Поделиться»", ChickengramConfig::hideShareButton, ChickengramConfig::setHideShareButton));
        items.add(check("Итоги до голосования", () -> NaConfig.INSTANCE.getShowVoteCountBeforeVote().Bool(), v -> NaConfig.INSTANCE.getShowVoteCountBeforeVote().setConfigBool(v)));
        items.add(UItem.asShadow("Итоги до голосования показывают результаты опроса до того, как вы проголосуете."));

        items.add(UItem.asHeader("Камера"));
        items.add(check("Основная камера в кружках", () -> NekoConfig.rearVideoMessages.Bool(), NekoConfig.rearVideoMessages::setConfigBool));
        items.add(check("Статичный зум", ChickengramConfig::staticZoom, ChickengramConfig::setStaticZoom));
        items.add(UItem.asShadow("При записи видеосообщений уровень приближения не будет сбрасываться, если отпустить пальцы."));

        items.add(UItem.asHeader("Фото"));
        items.add(check("Всегда отправлять в HD", ChickengramConfig::alwaysHd, ChickengramConfig::setAlwaysHd));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("Видео"));
        int seekIndex = 1;
        for (int i = 0; i < SEEK_VALUES.length; i++) {
            if (SEEK_VALUES[i] == ChickengramConfig.doubleTapSeek()) {
                seekIndex = i;
            }
        }
        final int selectedSeek = seekIndex;
        items.add(button(0, "Перемотка двойным нажатием", SEEK_TITLES[selectedSeek], v -> choose("Перемотка двойным нажатием", SEEK_TITLES, selectedSeek, index -> ChickengramConfig.setDoubleTapSeek(SEEK_VALUES[index]))));
        items.add(check("Включение звука кнопками громкости", ChickengramConfig::volumeUnmute, ChickengramConfig::setVolumeUnmute));
        items.add(check("Автопауза", () -> NekoConfig.autoPauseVideo.Bool(), NekoConfig.autoPauseVideo::setConfigBool));
        items.add(UItem.asShadow("Кнопки громкости включают звук в видео вместо изменения системной громкости. Автопауза останавливает видео, когда телефон заблокирован или приложение свёрнуто."));
    }

    private void setStickerShape(int shape) {
        ChickengramConfig.setStickerShape(shape);
        refresh();
    }

    private static String doubleTapTitle(int value) {
        for (int i = 0; i < DOUBLE_TAP_VALUES.length; i++) {
            if (DOUBLE_TAP_VALUES[i] == value) {
                return DOUBLE_TAP_TITLES[i];
            }
        }
        return DOUBLE_TAP_TITLES[0];
    }

    private void chooseDoubleTap(boolean outgoing) {
        final int count = outgoing ? DOUBLE_TAP_TITLES.length : DOUBLE_TAP_TITLES.length - 1;
        final String[] titles = new String[count];
        System.arraycopy(DOUBLE_TAP_TITLES, 0, titles, 0, count);
        final int current = (outgoing ? NaConfig.INSTANCE.getDoubleTapActionOutgoing() : NaConfig.INSTANCE.getDoubleTapActionIncoming()).Int();
        int selected = 0;
        for (int i = 0; i < count; i++) {
            if (DOUBLE_TAP_VALUES[i] == current) {
                selected = i;
            }
        }
        choose(outgoing ? "Исходящие сообщения" : "Входящие сообщения", titles, selected, index ->
            (outgoing ? NaConfig.INSTANCE.getDoubleTapActionOutgoing() : NaConfig.INSTANCE.getDoubleTapActionIncoming()).setConfigInt(DOUBLE_TAP_VALUES[index]));
    }
}
