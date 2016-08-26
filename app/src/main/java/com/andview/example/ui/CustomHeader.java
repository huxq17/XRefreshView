package com.andview.example.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.andview.example.DensityUtil;
import com.andview.refreshview.callback.IHeaderCallBack;

public class CustomHeader extends View implements IHeaderCallBack {

    private MaterialProgressDrawable mDrawable;
    private float mScale = 1f;
    private int[] colors = {0xFF0000FF, 0xFFFF7F00, 0xFF00FF00
            , 0xFF00FFFF, 0xFFFF0000, 0xFF8B00FF, 0xFFFFFF00};
    // Default background for the progress spinner
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    private int mPinnedTime;

    public CustomHeader(Context context, int pinnedTime) {
        super(context);
        initView();
        this.mPinnedTime = pinnedTime;
    }

    public CustomHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CustomHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mDrawable = new MaterialProgressDrawable(getContext(), this);
//        mDrawable.setBackgroundColor(CIRCLE_BG_LIGHT);
        mDrawable.setBackgroundColor(Color.WHITE);
        //圈圈颜色,可以是多种颜色
        mDrawable.setColorSchemeColors(colors);
        //设置圈圈的各种大小
//        mDrawable.updateSizes(MaterialProgressDrawable.LARGE);
        mDrawable.setCallback(this);
        int padding = DensityUtil.dip2px(getContext(), 10);
        setPadding(0, padding, 0, padding);
    }

    @Override
    public void invalidateDrawable(Drawable dr) {
        if (dr == mDrawable) {
            invalidate();
        } else {
            super.invalidateDrawable(dr);
        }
    }

    public void setColorSchemeColors(int[] colors) {
        mDrawable.setColorSchemeColors(colors);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = mDrawable.getIntrinsicHeight() + getPaddingTop()
                + getPaddingBottom();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        final int size = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int saveCount = canvas.save();
        Rect rect = mDrawable.getBounds();
        int l = getPaddingLeft() + (getMeasuredWidth() - mDrawable.getIntrinsicWidth()) / 2;
        canvas.translate(l, getPaddingTop());
        canvas.scale(mScale, mScale, rect.exactCenterX(), rect.exactCenterY());
        mDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public void onStateNormal() {
        mScale = 1f;
        isStop = false;
//        mDrawable.stop();
    }

    @Override
    public void onStateReady() {
        isStop = false;
    }

    @Override
    public void onStateRefreshing() {
        mDrawable.setAlpha(255);
        mDrawable.start();

    }

    @Override
    public void onStateFinish(boolean success) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                isStop = true;
                mDrawable.stop();
            }
        }, mPinnedTime);
    }

    private boolean isStop = false;

    @Override
    public void onHeaderMove(double headerMovePercent, int offsetY, int deltaY) {
        if (isStop) {
            return;
        }
        mDrawable.setAlpha((int) (255 * headerMovePercent));
        mDrawable.showArrow(true);

        float strokeStart = (float) ((headerMovePercent) * .8f);
        mDrawable.setStartEndTrim(0f, Math.min(0.8f, strokeStart));
        mDrawable.setArrowScale((float) Math.min(1f, headerMovePercent));

        // magic
        float rotation = (float) ((-0.25f + .4f * headerMovePercent + headerMovePercent * 2) * .5f);
        mDrawable.setProgressRotation(rotation);
        invalidate();

    }

    @Override
    public void setRefreshTime(long lastRefreshTime) {

    }

    @Override
    public void hide() {
        setVisibility(View.GONE);
    }

    @Override
    public void show() {
        setVisibility(View.VISIBLE);
    }

    @Override
    public int getHeaderHeight() {
        return getMeasuredHeight();
    }
}
