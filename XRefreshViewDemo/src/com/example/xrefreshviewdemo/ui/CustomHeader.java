package com.example.xrefreshviewdemo.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.andview.refreshview.callback.IHeaderCallBack;
import com.example.xrefreshviewdemo.DensityUtil;

public class CustomHeader extends View implements IHeaderCallBack {

	private MaterialProgressDrawable mDrawable;
	private float mScale = 1f;

	private Animation mScaleAnimation = new Animation() {
		@Override
		public void applyTransformation(float interpolatedTime, Transformation t) {
			mScale = 1f - interpolatedTime;
			mDrawable.setAlpha((int) (255 * mScale));
			invalidate();
		}
	};

	public CustomHeader(Context context) {
		super(context);
		initView();
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
		mDrawable.setBackgroundColor(Color.WHITE);
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
		int l = getPaddingLeft()
				+ (getMeasuredWidth() - mDrawable.getIntrinsicWidth()) / 2;
		canvas.translate(l, getPaddingTop());
		canvas.scale(mScale, mScale, rect.exactCenterX(), rect.exactCenterY());
		mDrawable.draw(canvas);
		canvas.restoreToCount(saveCount);
	}

	@Override
	public void onStateNormal() {
		mScale = 1f;
		mDrawable.stop();

	}

	@Override
	public void onStateReady() {

	}

	@Override
	public void onStateRefreshing() {
		mDrawable.setAlpha(255);
		mDrawable.start();

	}

	@Override
	public void onStateEnd() {
		// mDrawable.stop();
	}

	@Override
	public void onHeaderMove(double offset, int offsetY) {
		mDrawable.setAlpha((int) (255 * offset));
		mDrawable.showArrow(true);

		float strokeStart = (float) ((offset) * .8f);
		mDrawable.setStartEndTrim(0f, Math.min(0.8f, strokeStart));
		mDrawable.setArrowScale((float) Math.min(1f, offset));

		// magic
		float rotation = (float) ((-0.25f + .4f * offset + offset * 2) * .5f);
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
