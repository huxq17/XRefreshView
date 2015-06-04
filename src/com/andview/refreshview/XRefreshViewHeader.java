package com.andview.refreshview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class XRefreshViewHeader extends LinearLayout implements
		XRefreshHeaderViewBase {
	private RelativeLayout mContainer;
	private ImageView mArrowImageView;
	private ProgressBar mProgressBar;
	private TextView mHintTextView;
	private TextView mHeaderTimeTextView;
	private XRefreshViewState mState = XRefreshViewState.STATE_NORMAL;

	private Animation mRotateUpAnim;
	private Animation mRotateDownAnim;

	private final int ROTATE_ANIM_DURATION = 180;
	private RelativeLayout mHeaderViewContent;

	public XRefreshViewHeader(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public XRefreshViewHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public int height;

	private void initView(Context context) {
		mContainer = (RelativeLayout) LayoutInflater.from(context).inflate(
				R.layout.xrefreshview_header, null);
		addView(mContainer);
		setGravity(Gravity.BOTTOM);
		this.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@SuppressWarnings("deprecation")
					@SuppressLint("NewApi")
					@Override
					public void onGlobalLayout() {
						// 移除视图树监听器
						if (Build.VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
							getViewTreeObserver().removeGlobalOnLayoutListener(
									this);
						} else {
							getViewTreeObserver().removeOnGlobalLayoutListener(
									this);
						}
						height = mContainer.getMeasuredHeight();
						Log.i("head OnGloabal", "head OnGloabal height="
								+ height);
					}
				});
		mHeaderViewContent = (RelativeLayout) findViewById(R.id.xrefreshview_header_content);
		mArrowImageView = (ImageView) findViewById(R.id.xrefreshview_header_arrow);
		mHintTextView = (TextView) findViewById(R.id.xrefreshview_header_hint_textview);
		mHeaderTimeTextView = (TextView) findViewById(R.id.xrefreshview_header_time);
		mProgressBar = (ProgressBar) findViewById(R.id.xrefreshview_header_progressbar);

		mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateUpAnim.setFillAfter(true);
		mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateDownAnim.setFillAfter(true);
	}

	public void setRefreshTime(String time) {
		mHeaderTimeTextView.setText(time);
	}

	public void setState(XRefreshViewState state) {
		if (state == mState)
			return;

		if (state == XRefreshViewState.STATE_REFRESHING) {
			mArrowImageView.clearAnimation();
			mArrowImageView.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.VISIBLE);
		} else {
			mProgressBar.setVisibility(View.GONE);
			mArrowImageView.setVisibility(View.VISIBLE);
		}

		switch (state) {
		case STATE_NORMAL:
			if (mState == XRefreshViewState.STATE_READY) {
				mArrowImageView.startAnimation(mRotateDownAnim);
			}
			if (mState == XRefreshViewState.STATE_REFRESHING) {
				mArrowImageView.clearAnimation();
			}
			mHintTextView.setText(R.string.xrefreshview_header_hint_normal);
			break;
		case STATE_READY:
			if (mState != XRefreshViewState.STATE_READY) {
				mArrowImageView.clearAnimation();
				mArrowImageView.startAnimation(mRotateUpAnim);
				mHintTextView.setText(R.string.xrefreshview_header_hint_ready);
			}
			break;
		case STATE_REFRESHING:
			mHintTextView.setText(R.string.xrefreshview_header_hint_loading);
			break;
		default:
		}

		mState = state;
	}

	public int getVisiableHeight() {
		return mContainer.getHeight();
	}

	@Override
	public int getHeaderContentHeight() {
		return mHeaderViewContent.getMeasuredHeight();
	}

}
