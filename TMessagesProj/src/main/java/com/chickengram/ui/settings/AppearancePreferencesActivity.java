package com.chickengram.ui.settings;

import com.chickengram.ChickengramConfig;

import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;

import java.util.ArrayList;

import tw.nekomimi.nekogram.NekoConfig;
import tw.nekomimi.nekogram.NekoXConfig;
import xyz.nextalone.nagram.NaConfig;

public class AppearancePreferencesActivity extends BasePreferencesFragment {

    private static final String[] TAB_TITLES = { "Только названия", "Только иконки", "Названия и иконки" };
    private static final int[] TAB_TITLE_VALUES = { NekoXConfig.TITLE_TYPE_TEXT, NekoXConfig.TITLE_TYPE_ICON, NekoXConfig.TITLE_TYPE_MIX };

    private AvatarCornersPreview avatarPreview;
    private org.telegram.ui.Cells.AppIconsSelectorCell iconsCell;
    private boolean rebuildOnClose;

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (rebuildOnClose && parentLayout != null) {
            final org.telegram.ui.ActionBar.INavigationLayout layout = parentLayout;
            org.telegram.messenger.AndroidUtilities.runOnUIThread(() -> layout.rebuildAllFragmentViews(false, false));
        }
    }

    @Override
    protected CharSequence getTitle() {
        return "Внешний вид";
    }

    @Override
    protected void fill(ArrayList<UItem> items) {
        if (avatarPreview == null) {
            avatarPreview = new AvatarCornersPreview(getContext());
        }
        items.add(UItem.asHeader("Закругление аватарок"));
        items.add(UItem.asIntSlideView(1, 0, ChickengramConfig.avatarCorners(), ChickengramConfig.MAX_AVATAR_CORNERS,
            value -> value == 0 ? "Квадрат" : value == ChickengramConfig.MAX_AVATAR_CORNERS ? "Круг" : value + " dp",
            value -> {
                ChickengramConfig.setAvatarCorners(value);
                avatarPreview.invalidate();
                rebuildOnClose = true;
            }));
        items.add(UItem.asCustom(avatarPreview));
        items.add(UItem.asShadow("Форма аватарок во всём приложении: от квадрата до круга."));

        items.add(UItem.asHeader("Список чатов"));
        items.add(restartCheck("Принудительный снег", () -> NekoConfig.actionBarDecoration.Int() == 1, v -> NekoConfig.actionBarDecoration.setConfigInt(v ? 1 : 0)));
        items.add(check("Заголовок по центру", () -> NaConfig.INSTANCE.getCenterActionBarTitle().Bool(), v -> NaConfig.INSTANCE.getCenterActionBarTitle().setConfigBool(v)));
        items.add(check("Скрыть истории", () -> NaConfig.INSTANCE.getDisableStories().Bool(), v -> NaConfig.INSTANCE.getDisableStories().setConfigBool(v)));
        items.add(check("Скрыть плавающую кнопку", () -> NaConfig.INSTANCE.getDisableDialogsFloatingButton().Bool(), v -> NaConfig.INSTANCE.getDisableDialogsFloatingButton().setConfigBool(v)));
        items.add(check("Мини-аватарки отправителей", () -> NaConfig.INSTANCE.getShowUserIconsInChatsList().Bool(), v -> NaConfig.INSTANCE.getShowUserIconsInChatsList().setConfigBool(v)));
        items.add(UItem.asShadow("Падающий снег появится в верхней панели, боковом меню и на фоне чатов."));

        items.add(UItem.asHeader("Папки с чатами"));
        int tabIndex = 0;
        for (int i = 0; i < TAB_TITLE_VALUES.length; i++) {
            if (TAB_TITLE_VALUES[i] == NekoConfig.tabsTitleType.Int()) {
                tabIndex = i;
            }
        }
        final int selectedTab = tabIndex;
        items.add(button(0, "Заголовки папок", TAB_TITLES[selectedTab], v -> choose("Заголовки папок", TAB_TITLES, selectedTab, index -> {
            NekoConfig.tabsTitleType.setConfigInt(TAB_TITLE_VALUES[index]);
            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
        })));
        items.add(check("Скрыть вкладку «Все чаты»", () -> NekoConfig.hideAllTab.Bool(), v -> {
            NekoConfig.hideAllTab.setConfigBool(v);
            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
        }));
        items.add(UItem.asShadow("Иконки папок синхронизируются с вашим аккаунтом."));

        if (iconsCell == null) {
            iconsCell = new org.telegram.ui.Cells.AppIconsSelectorCell(getContext(), this, currentAccount);
        }
        items.add(UItem.asHeader("Иконка приложения"));
        items.add(UItem.asCustom(iconsCell));
        items.add(UItem.asShadow("Иконка на рабочем столе. Лаунчер может обновить её не сразу."));

        items.add(button(R.drawable.msg_customize, "Pill Stack", null, v -> presentFragment(new PillStackPreferencesActivity())));
        items.add(UItem.asShadow("Интерактивные кнопки в поле поиска на главном экране: призрак, Избранное, Архив, ИИ-чат."));

        items.add(UItem.asHeader("Наборы иконок"));
        items.add(restartCheck("Набор иконок «Solar»", ChickengramConfig::solarIcons, ChickengramConfig::setSolarIcons));
        items.add(UItem.asShadow("Заменяет иконки меню, кнопок и папок на набор «Solar», как в exteraGram."));

        items.add(UItem.asHeader("Внешний вид"));
        items.add(radio("Круглая кнопка «+»", !ChickengramConfig.squareFab(), v -> {
            ChickengramConfig.setSquareFab(false);
            refresh();
            showRestartBulletin();
        }));
        items.add(radio("Квадратная кнопка «+»", ChickengramConfig.squareFab(), v -> {
            ChickengramConfig.setSquareFab(true);
            refresh();
            showRestartBulletin();
        }));
        items.add(restartCheck("Системные шрифты", () -> NekoConfig.typeface.Bool(), NekoConfig.typeface::setConfigBool));
        items.add(restartCheck("Системные эмодзи", () -> NekoConfig.useSystemEmoji.Bool(), NekoConfig.useSystemEmoji::setConfigBool));
        items.add(restartCheck("Переключатели в стиле Material 3", () -> NaConfig.INSTANCE.getSwitchStyle().Int() != 0, v -> NaConfig.INSTANCE.getSwitchStyle().setConfigInt(v ? 1 : 0)));
        items.add(restartCheck("Заголовок чата в стиле Material 3", () -> NaConfig.INSTANCE.getMaterialDesign3ChatHeader().Bool(), v -> NaConfig.INSTANCE.getMaterialDesign3ChatHeader().setConfigBool(v)));
        items.add(check("Различные темы в чатах", () -> !NaConfig.INSTANCE.getDisableCustomWallpaperUser().Bool(), v -> {
            NaConfig.INSTANCE.getDisableCustomWallpaperUser().setConfigBool(!v);
            NaConfig.INSTANCE.getDisableCustomWallpaperChannel().setConfigBool(!v);
        }));
        items.add(check("«Липкая» анимация аватарок", () -> !NaConfig.INSTANCE.getDisableGooeyAvatarAnimation().Bool(), v -> NaConfig.INSTANCE.getDisableGooeyAvatarAnimation().setConfigBool(!v)));
        items.add(check("Отключить разделители", ChickengramConfig::disableDividers, v -> {
            ChickengramConfig.setDisableDividers(v);
            if (Theme.dividerPaint != null) {
                Theme.dividerPaint.setColor(v ? 0 : Theme.getColor(Theme.key_divider));
            }
        }));
        items.add(UItem.asShadow("Разделители — тонкие линии между пунктами списков."));

        items.add(UItem.asHeader("Секции"));
        final int[] radiusValues = { 0, 8, 12, 16, 20, 24, 28 };
        final int currentRadius = ChickengramConfig.sectionRadius() < 0 ? 16 : ChickengramConfig.sectionRadius();
        int radiusIndex = 3;
        for (int i = 0; i < radiusValues.length; i++) {
            if (radiusValues[i] == currentRadius) {
                radiusIndex = i;
            }
        }
        items.add(UItem.asSlideView(new String[] { "Откл.", "8", "12", "16", "20", "24", "Макс" }, radiusIndex, index -> ChickengramConfig.setSectionRadius(radiusValues[index])));
        items.add(check("Отделить заголовки", ChickengramConfig::separateHeaders, ChickengramConfig::setSeparateHeaders));
        items.add(UItem.asShadow("Скругление карточек в настройках. «Откл.» — плоские секции на всю ширину. Изменения видны при повторном открытии экрана."));

        items.add(UItem.asHeader("Настройки размытия"));
        items.add(check("Блики на стекле", () -> !NaConfig.INSTANCE.getDisableGlareEffects().Bool(), v -> NaConfig.INSTANCE.getDisableGlareEffects().setConfigBool(!v)));
        items.add(check("Принудительное размытие", () -> NekoConfig.forceBlurInChat.Bool(), NekoConfig.forceBlurInChat::setConfigBool));
        items.add(UItem.asShadow("Эффекты размытия будут применяться ко всем темам, переопределяя их стандартные настройки."));
    }
}
