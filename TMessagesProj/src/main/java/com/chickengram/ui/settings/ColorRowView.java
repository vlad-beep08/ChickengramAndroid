package com.chickengram.ui.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class ColorRowView extends View {

    public interface Listener {
        void onColor(int index);
    }

    private final int[] colors;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Listener listener;
    private int selected;

    public ColorRowView(Context context, int[] colors, int selected, Listener listener) {
        super(context);
        this.colors = colors;
        this.selected = selected;
        this.listener = listener;
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(AndroidUtilities.dp(2));
        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
    }

    public void setSelected(int index) {
        selected = index;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY));
    }

    private float step() {
        return (getWidth() - AndroidUtilities.dp(32)) / (float) colors.length;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float step = step();
        final float radius = Math.min(AndroidUtilities.dp(15), step / 2f - AndroidUtilities.dp(3));
        final float cy = getHeight() / 2f;
        for (int i = 0; i < colors.length; i++) {
            final float cx = AndroidUtilities.dp(16) + step * i + step / 2f;
            paint.setColor(colors[i]);
            if (i == selected) {
                ring.setColor(colors[i]);
                canvas.drawCircle(cx, cy, radius, ring);
                canvas.drawCircle(cx, cy, radius - AndroidUtilities.dp(4), paint);
            } else {
                canvas.drawCircle(cx, cy, radius, paint);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            final int index = (int) ((event.getX() - AndroidUtilities.dp(16)) / step());
            if (index >= 0 && index < colors.length) {
                setSelected(index);
                listener.onColor(index);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}
