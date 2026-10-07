package com.chickengram.ui.settings;

import com.chickengram.ChickengramConfig;

import org.telegram.ui.Components.UItem;

import java.util.ArrayList;

public class PillStackPreferencesActivity extends BasePreferencesFragment {

    @Override
    protected CharSequence getTitle() {
        return "Pill Stack";
    }

    @Override
    protected void fill(ArrayList<UItem> items) {
        items.add(UItem.asHeader("Кнопки в поле поиска"));
        items.add(restartCheck("Режим призрака", ChickengramConfig::pillGhost, ChickengramConfig::setPillGhost));
        items.add(restartCheck("Избранное", ChickengramConfig::pillSaved, ChickengramConfig::setPillSaved));
        items.add(restartCheck("Архив", ChickengramConfig::pillArchive, ChickengramConfig::setPillArchive));
        items.add(restartCheck("ИИ-чат", ChickengramConfig::pillAi, ChickengramConfig::setPillAi));
        items.add(UItem.asShadow("Кнопки появляются в поле поиска над списком чатов. Призрак включается и выключается одним нажатием и подсвечивается, когда активен. На маленьких экранах лучше оставить две-три кнопки."));
    }
}
