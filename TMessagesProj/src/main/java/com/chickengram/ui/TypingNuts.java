package com.chickengram.ui;

import static org.telegram.messenger.AndroidUtilities.dpf2;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.chickengram.ChickengramConfig;

import java.util.ArrayList;
import java.util.Random;

public final class TypingNuts {

    private static final int NUTS_PER_STROKE = 2;
    private static final int NUT_BUMPS = 3;
    private static final long NUT_DURATION = 760;
    private static final float NUT_SIZE_MIN = 10;
    private static final float NUT_SIZE_EXTRA = 4;
    private static final float NUT_ASPECT = 1.2f;
    private static final float NUT_SPREAD_X = 60;
    private static final float NUT_LIFT_MIN = 40;
    private static final float NUT_LIFT_EXTRA = 45;
    private static final float NUT_GRAVITY = 180;
    private static final float NUT_SPIN = 420f;
    private static final float NUT_FADE_FROM = 0.65f;
    private static final float NUT_BUMP_SIZE = 0.22f;
    private static final int NUT_SHINE = 0x96FFFFFF;

    private static final Random random = new Random();
    private static final int[] editLocation = new int[2];
    private static final int[] layerLocation = new int[2];

    private TypingNuts() {
    }

    public static void spawn(EditText edit) {
        if (edit == null || !ChickengramConfig.typingNuts() || !ValueAnimator.areAnimatorsEnabled() || !edit.isAttachedToWindow()) {
            return;
        }
        final Layout layout = edit.getLayout();
        if (layout == null) {
            return;
        }
        final int offset = Math.max(0, Math.min(edit.getSelectionEnd(), edit.length()));
        final int line = layout.getLineForOffset(offset);
        final float x = layout.getPrimaryHorizontal(offset) + edit.getTotalPaddingLeft() - edit.getScrollX();
        final float y = (layout.getLineTop(line) + layout.getLineBottom(line)) / 2f + edit.getTotalPaddingTop() - edit.getScrollY();
        final NutLayer nutLayer = layerFor(edit);
        if (nutLayer == null) {
            return;
        }
        edit.getLocationInWindow(editLocation);
        nutLayer.getLocationInWindow(layerLocation);
        nutLayer.spawn(editLocation[0] - layerLocation[0] + x, editLocation[1] - layerLocation[1] + y);
    }

    private static NutLayer layerFor(View view) {
        final View root = view.getRootView();
        if (!(root instanceof ViewGroup)) {
            return null;
        }
        final ViewGroup group = (ViewGroup) root;
        for (int i = group.getChildCount() - 1; i >= 0; i--) {
            final View child = group.getChildAt(i);
            if (child instanceof NutLayer) {
                if (i != group.getChildCount() - 1) {
                    child.bringToFront();
                }
                return (NutLayer) child;
            }
        }
        final NutLayer layer = new NutLayer(view.getContext());
        group.addView(layer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return layer;
    }

    private static final class Nut {
        float originX;
        float originY;
        float velocityX;
        float velocityY;
        float size;
        float angle;
        float spin;
        long started;
        Shader shader;
        final float[] bumps = new float[NUT_BUMPS * 2];
    }

    private static final class NutLayer extends View {

        private final ArrayList<Nut> nuts = new ArrayList<>();
        private final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        NutLayer(Context context) {
            super(context);
            setClickable(false);
            setFocusable(false);
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            setWillNotDraw(false);
            shinePaint.setColor(NUT_SHINE);
        }

        @Override
        public boolean onTouchEvent(android.view.MotionEvent event) {
            return false;
        }

        void spawn(float x, float y) {
            final long now = SystemClock.uptimeMillis();
            for (int i = 0; i < NUTS_PER_STROKE; i++) {
                final Nut nut = new Nut();
                nut.originX = x;
                nut.originY = y;
                nut.velocityX = (random.nextFloat() * 2f - 1f) * dpf2(NUT_SPREAD_X);
                nut.velocityY = -dpf2(NUT_LIFT_MIN) - random.nextFloat() * dpf2(NUT_LIFT_EXTRA);
                nut.size = dpf2(NUT_SIZE_MIN) + random.nextFloat() * dpf2(NUT_SIZE_EXTRA);
                nut.angle = random.nextFloat() * 360f;
                nut.spin = (random.nextFloat() * 2f - 1f) * NUT_SPIN;
                nut.started = now;
                final float width = nut.size * NUT_ASPECT;
                nut.shader = new RadialGradient(-width * 0.2f, -nut.size * 0.25f, Math.max(1f, width * 0.8f), new int[] { 0xFFD9F284, 0xFF93C83E, 0xFF56861F }, new float[] { 0f, 0.5f, 1f }, Shader.TileMode.CLAMP);
                for (int b = 0; b < NUT_BUMPS; b++) {
                    final double angle = random.nextFloat() * Math.PI * 2;
                    nut.bumps[b * 2] = (float) Math.cos(angle) * 0.8f;
                    nut.bumps[b * 2 + 1] = (float) Math.sin(angle) * 0.8f;
                }
                nuts.add(nut);
            }
            postInvalidateOnAnimation();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (nuts.isEmpty()) {
                return;
            }
            final long now = SystemClock.uptimeMillis();
            for (int i = nuts.size() - 1; i >= 0; i--) {
                final Nut nut = nuts.get(i);
                final float t = (now - nut.started) / (float) NUT_DURATION;
                if (t >= 1f) {
                    nuts.remove(i);
                    continue;
                }
                drawNut(canvas, nut, Math.max(0f, t));
            }
            if (!nuts.isEmpty()) {
                postInvalidateOnAnimation();
            }
        }

        private void drawNut(Canvas canvas, Nut nut, float t) {
            final float width = nut.size * NUT_ASPECT;
            final float height = nut.size;
            final float cx = nut.originX + nut.velocityX * t;
            final float cy = nut.originY + nut.velocityY * t + dpf2(NUT_GRAVITY) * t * t;
            final float opacity = t < NUT_FADE_FROM ? 1f : Math.max(1f - (t - NUT_FADE_FROM) / (1f - NUT_FADE_FROM), 0f);
            final int alpha = (int) (0xFF * opacity);

            canvas.save();
            canvas.translate(cx, cy);
            canvas.rotate(nut.angle + nut.spin * t);
            bodyPaint.setShader(nut.shader);
            bodyPaint.setAlpha(alpha);
            rect.set(-width / 2f, -height / 2f, width / 2f, height / 2f);
            canvas.drawOval(rect, bodyPaint);
            final float bumpRadius = height * NUT_BUMP_SIZE;
            for (int b = 0; b < NUT_BUMPS; b++) {
                canvas.drawCircle(nut.bumps[b * 2] * width / 2f, nut.bumps[b * 2 + 1] * height / 2f, bumpRadius, bodyPaint);
            }
            shinePaint.setAlpha((int) (Math.min(0xFF, NUT_SHINE >>> 24) * opacity));
            rect.set(-width * 0.3f, -height * 0.34f, -width * 0.3f + width * 0.26f, -height * 0.34f + height * 0.2f);
            canvas.drawOval(rect, shinePaint);
            canvas.restore();
        }
    }
}
