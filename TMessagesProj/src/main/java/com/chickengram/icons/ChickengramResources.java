package com.chickengram.icons;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class ChickengramResources extends Resources {

    private final Resources base;

    @SuppressWarnings("deprecation")
    public ChickengramResources(Resources base) {
        super(base.getAssets(), base.getDisplayMetrics(), base.getConfiguration());
        this.base = base;
    }

    public static int map(int id) {
        return SolarIcons.MAP.get(id, id);
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
        return base.getDrawableForDensity(map(id), density, theme);
    }
}
