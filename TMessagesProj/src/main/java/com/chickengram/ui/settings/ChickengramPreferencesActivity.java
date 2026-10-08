package com.chickengram.ui.settings;

import org.telegram.messenger.BuildVars;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.Components.UItem;

import java.util.ArrayList;

public class ChickengramPreferencesActivity extends BasePreferencesFragment {

    private PreferencesHeaderView header;

    @Override
    protected CharSequence getTitle() {
        return "Настройки Чикенграма";
    }

    @Override
    protected void fill(ArrayList<UItem> items) {
        if (header == null) {
            header = new PreferencesHeaderView(getContext(), R.mipmap.ic_launcher_nagram_round, 0, 0, "Чикенграм", BuildVars.BUILD_VERSION_STRING);
        }
        items.add(UItem.asCustomShadow(header));
        items.add(UItem.asHeader("Категории"));
        items.add(button(R.drawable.msg_settings, "Основные", null, v -> presentFragment(new GeneralPreferencesActivity())));
        items.add(button(R.drawable.msg_palette, "Внешний вид", null, v -> presentFragment(new AppearancePreferencesActivity())));
        items.add(button(R.drawable.msg_discussion, "Чаты", null, v -> presentFragment(new ChatsPreferencesActivity())));
        items.add(button(R.drawable.msg_fave, "Другое", null, v -> presentFragment(new OtherPreferencesActivity())));
        items.add(button(R.drawable.msg_bot, "ИИ-чат", null, v -> presentFragment(new AiChatSettingsActivity())));
        items.add(UItem.asShadow(null));
        items.add(UItem.asHeader("Ссылки"));
        items.add(button(R.drawable.msg_link, "Исходный код", "GitHub", v -> Browser.openUrl(getParentActivity(), "https://github.com/vlad-beep08/ChickengramAndroid")));
        items.add(button(R.drawable.chickengram_ghost_outline, "Призрак и шпион", null, v -> presentFragment(new SpyPreferencesActivity())));
        items.add(UItem.asShadow(null));
    }
}
