package com.chickengram.ui.settings;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class PreferencesHeaderView extends LinearLayout {

    public PreferencesHeaderView(Context context, int iconRes, int glyphRes, int glyphBackground, String title, String subtitle) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setPadding(0, AndroidUtilities.dp(20), 0, AndroidUtilities.dp(16));

        final ImageView icon = new ImageView(context);
        icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (glyphRes != 0) {
            final GradientDrawable background = new GradientDrawable();
            background.setColor(glyphBackground);
            background.setCornerRadius(AndroidUtilities.dp(22));
            icon.setBackground(background);
            icon.setScaleType(ImageView.ScaleType.CENTER);
            icon.setImageResource(glyphRes);
            icon.setColorFilter(new PorterDuffColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_IN));
        } else {
            icon.setImageResource(iconRes);
            icon.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), AndroidUtilities.dp(22));
                }
            });
            icon.setClipToOutline(true);
        }
        addView(icon, LayoutHelper.createLinear(84, 84, Gravity.CENTER_HORIZONTAL));

        final TextView name = new TextView(context);
        name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
        name.setTypeface(AndroidUtilities.bold());
        name.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        name.setGravity(Gravity.CENTER);
        name.setText(title);
        addView(name, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 14, 0, 0));

        if (subtitle != null) {
            final TextView version = new TextView(context);
            version.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            version.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            version.setGravity(Gravity.CENTER);
            version.setText(subtitle);
            addView(version, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 4, 0, 0));
        }
    }
}
