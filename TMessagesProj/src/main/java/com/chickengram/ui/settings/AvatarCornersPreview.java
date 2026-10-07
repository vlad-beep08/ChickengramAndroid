package com.chickengram.ui.settings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.chickengram.ChickengramConfig;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class AvatarCornersPreview extends View {

    private final Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint avatarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint onlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public AvatarCornersPreview(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(104), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float pad = AndroidUtilities.dp(16);
        cardPaint.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
        rect.set(pad, AndroidUtilities.dp(10), getWidth() - pad, getHeight() - AndroidUtilities.dp(10));
        canvas.drawRoundRect(rect, AndroidUtilities.dp(14), AndroidUtilities.dp(14), cardPaint);

        final float size = AndroidUtilities.dp(56);
        final float left = pad + AndroidUtilities.dp(14);
        final float top = (getHeight() - size) / 2f;
        final float radius = size / 2f * ChickengramConfig.avatarCorners() / ChickengramConfig.MAX_AVATAR_CORNERS;
        avatarPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        avatarPaint.setAlpha(140);
        rect.set(left, top, left + size, top + size);
        canvas.drawRoundRect(rect, radius, radius, avatarPaint);

        onlinePaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
        canvas.drawCircle(left + size - AndroidUtilities.dp(6), top + size - AndroidUtilities.dp(6), AndroidUtilities.dp(6), onlinePaint);

        linePaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        linePaint.setAlpha(90);
        final float textLeft = left + size + AndroidUtilities.dp(14);
        final float right = getWidth() - pad - AndroidUtilities.dp(14);
        final float h = AndroidUtilities.dp(8);
        rect.set(textLeft, top + AndroidUtilities.dp(10), textLeft + (right - textLeft) * 0.55f, top + AndroidUtilities.dp(10) + h);
        canvas.drawRoundRect(rect, h / 2, h / 2, linePaint);
        rect.set(right - AndroidUtilities.dp(36), top + AndroidUtilities.dp(10), right, top + AndroidUtilities.dp(10) + h);
        canvas.drawRoundRect(rect, h / 2, h / 2, linePaint);
        rect.set(textLeft, top + AndroidUtilities.dp(26), right - AndroidUtilities.dp(20), top + AndroidUtilities.dp(26) + h);
        canvas.drawRoundRect(rect, h / 2, h / 2, linePaint);
        rect.set(textLeft, top + AndroidUtilities.dp(40), textLeft + (right - textLeft) * 0.7f, top + AndroidUtilities.dp(40) + h);
        canvas.drawRoundRect(rect, h / 2, h / 2, linePaint);
    }
}
