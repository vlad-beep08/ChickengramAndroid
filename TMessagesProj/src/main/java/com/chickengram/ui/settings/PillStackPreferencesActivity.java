package com.chickengram.ui.settings;

import com.chickengram.ChickengramConfig;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.Components.UItem;

import java.util.ArrayList;

public class PillStackPreferencesActivity extends BasePreferencesFragment {

    private boolean rebuildOnClose;

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (rebuildOnClose && parentLayout != null) {
            final INavigationLayout layout = parentLayout;
            AndroidUtilities.runOnUIThread(() -> layout.rebuildAllFragmentViews(false, false));
        }
    }

    @Override
    protected CharSequence getTitle() {
        return "Pill Stack";
    }

    @Override
    protected void fill(ArrayList<UItem> items) {
        items.add(UItem.asHeader("Кнопки в поле поиска"));
        items.add(pill("Режим призрака", ChickengramConfig::pillGhost, ChickengramConfig::setPillGhost));
        items.add(pill("Избранное", ChickengramConfig::pillSaved, ChickengramConfig::setPillSaved));
        items.add(pill("Архив", ChickengramConfig::pillArchive, ChickengramConfig::setPillArchive));
        items.add(pill("ИИ-чат", ChickengramConfig::pillAi, ChickengramConfig::setPillAi));
        items.add(UItem.asShadow("Кнопки появляются в поле поиска над списком чатов. Призрак включается и выключается одним нажатием и подсвечивается, когда активен. На маленьких экранах лучше оставить две-три кнопки."));
    }

    private UItem pill(String title, Getter getter, Setter setter) {
        return check(title, getter, value -> {
            setter.set(value);
            rebuildOnClose = true;
        });
    }
}
