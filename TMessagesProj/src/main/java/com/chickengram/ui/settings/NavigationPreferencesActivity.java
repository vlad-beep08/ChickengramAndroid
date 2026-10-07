package com.chickengram.ui.settings;

import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Components.UItem;

import java.util.ArrayList;

import tw.nekomimi.nekogram.NekoConfig;
import tw.nekomimi.nekogram.config.ConfigItem;
import xyz.nextalone.nagram.NaConfig;

public class NavigationPreferencesActivity extends BasePreferencesFragment {

    @Override
    protected CharSequence getTitle() {
        return "Навигация в приложении";
    }

    @Override
    protected void fill(ArrayList<UItem> items) {
        items.add(UItem.asHeader("Меню главного экрана"));
        items.add(item("Сменить тему", NaConfig.INSTANCE.getCustomDialogsMenuTheme()));
        items.add(item("Создать группу", NaConfig.INSTANCE.getCustomDialogsMenuNewGroup()));
        items.add(item("Новое сообщение", NaConfig.INSTANCE.getCustomDialogsMenuNewMessage()));
        items.add(item("Избранное", NaConfig.INSTANCE.getCustomDialogsMenuSavedMessages()));
        items.add(item("Настройки", NaConfig.INSTANCE.getCustomDialogsMenuSettings()));
        items.add(item("Прокси", NaConfig.INSTANCE.getCustomDialogsMenuProxy()));
        items.add(item("Добавить аккаунт", NaConfig.INSTANCE.getCustomDialogsMenuAccount()));
        items.add(check("Кнопка режима призрака", () -> NekoConfig.showGhostToggleInDrawer, v -> {
            NekoConfig.putBoolean("showGhostToggleInDrawer", NekoConfig.showGhostToggleInDrawer = v);
            NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
        }));
        items.add(check("Недавние чаты в меню", () -> NaConfig.INSTANCE.getShowRecentChatsInSidebar().Bool(), v -> NaConfig.INSTANCE.getShowRecentChatsInSidebar().setConfigBool(v)));
        items.add(UItem.asShadow("Пункты меню, которое открывается кнопкой в левом верхнем углу главного экрана."));
    }

    private UItem item(String title, ConfigItem config) {
        return check(title, config::Bool, config::setConfigBool);
    }
}
