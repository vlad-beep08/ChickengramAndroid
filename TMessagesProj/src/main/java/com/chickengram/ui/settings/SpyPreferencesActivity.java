package com.chickengram.ui.settings;

import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;

import java.util.ArrayList;

import tw.nekomimi.nekogram.settings.RegexFiltersSettingActivity;

public class SpyPreferencesActivity extends BasePreferencesFragment {

    private PreferencesHeaderView header;

    @Override
    protected CharSequence getTitle() {
        return "Призрак и шпион";
    }

    @Override
    protected void fill(ArrayList<UItem> items) {
        if (header == null) {
            header = new PreferencesHeaderView(getContext(), 0, R.drawable.ghost, 0xFF6B45E0, "Призрак и шпион", "Чикенграм");
        }
        items.add(UItem.asCustom(header));
        items.add(UItem.asHeader("Категории"));
        items.add(button(R.drawable.ghost, "Режим призрака", null, v -> presentFragment(new GhostPreferencesActivity())));
        items.add(button(R.drawable.msg_secret, "Шпион", null, v -> presentFragment(new SpyStoragePreferencesActivity())));
        items.add(button(R.drawable.msg_customize, "Фильтры", null, v -> presentFragment(new RegexFiltersSettingActivity())));
        items.add(button(R.drawable.msg_palette, "Кастомизация", null, v -> presentFragment(new SpyCustomizationActivity())));
        items.add(UItem.asShadow(null));
    }
}
