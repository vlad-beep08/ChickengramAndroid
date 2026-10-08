package com.chickengram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.PeerColorActivity;
import org.telegram.ui.Stars.StarGiftPatterns;

public class SettingsCover extends FrameLayout {

    public static final int HEIGHT_DP = 214;

    private final int currentAccount;
    private final Theme.ResourcesProvider resourcesProvider;
    private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable pattern;
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect anchorRect = new Rect();
    private final RectF avatarRect = new RectF();

    private View avatarAnchor;
    private int topInset;
    private int color1;
    private int color2;
    private boolean hasPattern;
    private RadialGradient gradient;
    private int gradientColor1;
    private int gradientColor2;
    private float gradientX;
    private float gradientY;

    public SettingsCover(Context context, int currentAccount, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.currentAccount = currentAccount;
        this.resourcesProvider = resourcesProvider;
        setWillNotDraw(false);
        pattern = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(this, false, dp(20), AnimatedEmojiDrawable.CACHE_TYPE_ALERT_PREVIEW_STATIC);
        update(null);
    }

    public void setAvatarAnchor(View view) {
        avatarAnchor = view;
    }

    public void setTopInset(int inset) {
        if (topInset != inset) {
            topInset = inset;
            setPadding(0, inset, 0, 0);
            requestLayout();
        }
    }

    public int getTopInset() {
        return topInset;
    }

    public boolean isLight() {
        return AndroidUtilities.computePerceivedBrightness(ColorUtils.blendARGB(color1, color2, 0.5f)) > 0.62f;
    }

    public void update(TLRPC.User user) {
        MessagesController.PeerColor peerColor = user == null ? null : MessagesController.PeerColor.fromCollectible(user.emoji_status);
        if (peerColor == null && user != null) {
            final MessagesController.PeerColors peerColors = MessagesController.getInstance(currentAccount).profilePeerColors;
            peerColor = peerColors == null ? null : peerColors.getColor(UserObject.getProfileColorId(user));
        }
        int patternColor;
        if (peerColor != null) {
            final boolean dark = Theme.isCurrentThemeDark();
            color1 = peerColor.getBgColor1(dark);
            color2 = peerColor.getBgColor2(dark);
            patternColor = peerColor.patternColor != 0 ? peerColor.patternColor : PeerColorActivity.adaptProfileEmojiColor(color1);
        } else {
            final int accent = Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider);
            color1 = ColorUtils.blendARGB(accent, Color.BLACK, 0.78f);
            color2 = ColorUtils.blendARGB(accent, Color.BLACK, 0.5f);
            patternColor = PeerColorActivity.adaptProfileEmojiColor(color1);
        }
        final long emojiId = user == null ? 0 : UserObject.getProfileEmojiId(user);
        hasPattern = emojiId != 0 && emojiId != -1;
        if (hasPattern) {
            pattern.set(emojiId, false);
        } else {
            pattern.set((Drawable) null, false);
        }
        pattern.setColor(patternColor);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(topInset + dp(HEIGHT_DP), MeasureSpec.EXACTLY)
        );
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        pattern.attach();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        pattern.detach();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        updateAvatarRect();
        final float cx = avatarRect.isEmpty() ? getWidth() / 2f : avatarRect.centerX();
        final float cy = avatarRect.isEmpty() ? topInset + dp(70) : avatarRect.centerY();
        if (gradient == null || gradientColor1 != color1 || gradientColor2 != color2 || gradientX != cx || gradientY != cy) {
            gradient = new RadialGradient(gradientX = cx, gradientY = cy, Math.max(dp(240), getWidth() * 0.75f), new int[] { gradientColor2 = color2, gradientColor1 = color1 }, new float[] { 0, 1 }, Shader.TileMode.CLAMP);
            backgroundPaint.setShader(gradient);
        }
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        if (hasPattern && !avatarRect.isEmpty()) {
            canvas.save();
            canvas.clipRect(0, 0, getWidth(), getHeight());
            StarGiftPatterns.drawProfileAnimatedPattern(canvas, pattern, getWidth(), 0, 1f, avatarRect, 1f);
            canvas.restore();
        }
    }

    public static class StatusView extends View {

        private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable drawable;
        private boolean empty = true;

        public StatusView(Context context) {
            super(context);
            drawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(this, dp(24), AnimatedEmojiDrawable.CACHE_TYPE_EMOJI_STATUS);
        }

        public void set(int account, TLRPC.User user, int color) {
            drawable.setCurrentAccount(account);
            drawable.setColor(color);
            final Long emojiStatusId = UserObject.getEmojiStatusDocumentId(user);
            if (emojiStatusId != null) {
                drawable.set(emojiStatusId, true);
                empty = false;
            } else if (user != null && user.premium) {
                drawable.set(getContext().getResources().getDrawable(org.telegram.messenger.R.drawable.msg_premium_liststar).mutate(), true);
                empty = false;
            } else {
                drawable.set((Drawable) null, true);
                empty = true;
            }
            setVisibility(empty ? GONE : VISIBLE);
            invalidate();
        }

        public boolean isEmpty() {
            return empty;
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            drawable.attach();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            drawable.detach();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            final int size = dp(24);
            final int left = (getWidth() - size) / 2;
            final int top = (getHeight() - size) / 2;
            drawable.setBounds(left, top, left + size, top + size);
            drawable.draw(canvas);
        }
    }

    private void updateAvatarRect() {
        if (avatarAnchor == null || avatarAnchor.getParent() == null) {
            avatarRect.setEmpty();
            return;
        }
        avatarAnchor.getDrawingRect(anchorRect);
        try {
            offsetDescendantRectToMyCoords(avatarAnchor, anchorRect);
            avatarRect.set(anchorRect);
        } catch (IllegalArgumentException e) {
            avatarRect.setEmpty();
        }
    }
}
