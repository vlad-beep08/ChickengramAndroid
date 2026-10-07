package com.chickengram.ui;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import org.telegram.messenger.AndroidUtilities;

public class TitleTextDrawable extends Drawable {

    private final TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final String text;

    public TitleTextDrawable(String text, float sizeDp) {
        this.text = text;
        paint.setColor(0xFFFFFFFF);
        paint.setTextSize(AndroidUtilities.dp(sizeDp));
        paint.setTypeface(AndroidUtilities.bold());
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect bounds = getBounds();
        final float baseline = bounds.centerY() - (paint.descent() + paint.ascent()) / 2f;
        canvas.drawText(text, bounds.left, baseline, paint);
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) Math.ceil(paint.measureText(text));
    }

    @Override
    public int getIntrinsicHeight() {
        final Paint.FontMetrics metrics = paint.getFontMetrics();
        return (int) Math.ceil(metrics.descent - metrics.ascent);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
