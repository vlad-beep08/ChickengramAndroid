package com.chickengram.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextPaint;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;

public class QuickPhraseButton extends View {

    public static final String PHRASE = "жопа";
    private static final String LABEL = "ЖОПА";
    private static final int TOP_COLOR = 0xFFFF5A4E;
    private static final int BOTTOM_COLOR = 0xFFC9221A;
    private static final int GLOSS_COLOR = 0x40FFFFFF;

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gloss = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint text = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final RectF shine = new RectF();
    private float shaderTop = -1;

    public QuickPhraseButton(Context context) {
        super(context);
        text.setColor(Color.WHITE);
        text.setTypeface(AndroidUtilities.bold());
        text.setTextSize(AndroidUtilities.dp(8.5f));
        text.setTextAlign(Paint.Align.CENTER);
        gloss.setColor(GLOSS_COLOR);
        setContentDescription(LABEL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float size = AndroidUtilities.dp(30);
        final float radius = AndroidUtilities.dp(8);
        final float left = (getWidth() - size) / 2f;
        final float top = (getHeight() - size) / 2f;
        rect.set(left, top, left + size, top + size);
        if (shaderTop != top) {
            shaderTop = top;
            fill.setShader(new LinearGradient(
                0, rect.top, 0, rect.bottom,
                TOP_COLOR, BOTTOM_COLOR, Shader.TileMode.CLAMP));
        }
        canvas.drawRoundRect(rect, radius, radius, fill);
        shine.set(rect.left + AndroidUtilities.dp(2), rect.top + AndroidUtilities.dp(2), rect.right - AndroidUtilities.dp(2), rect.centerY());
        canvas.drawRoundRect(shine, radius - AndroidUtilities.dp(2), radius - AndroidUtilities.dp(2), gloss);
        final float baseline = rect.centerY() - (text.descent() + text.ascent()) / 2f;
        canvas.drawText(LABEL, rect.centerX(), baseline, text);
    }
}
