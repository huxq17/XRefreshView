/**
 * @file XFooterView.java
 * @create Mar 31, 2012 9:33:43 PM
 * @author Maxwin
 * @description Xrefreshview's footer
 */
package com.andview.refreshview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class XRefreshViewFooter extends LinearLayout {
	private Context mContext;

	private View mContentView;
	private View mProgressBar;
	private TextView mHintView;

	public XRefreshViewFooter(Context context) {
		super(context);
		initView(context);
	}

	public XRefreshViewFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public void setState(XRefreshViewState state) {
		mHintView.setVisibility(View.INVISIBLE);
		mProgressBar.setVisibility(View.INVISIBLE);
		mHintView.setVisibility(View.INVISIBLE);
		if (state == XRefreshViewState.STATE_READY) {
			// mHintView.setVisibility(View.VISIBLE);
			// mHintView.setText(R.string.xrefreshview_footer_hint_ready);
		} else if (state == XRefreshViewState.STATE_LOADING) {
			mProgressBar.setVisibility(View.VISIBLE);
		} else {
			// mHintView.setVisibility(View.VISIBLE);
			// mHintView.setText(R.string.xrefreshview_footer_hint_normal);
		}
	}

	public void setBottomMargin(int height) {
		Log.i("footView", "footView is Visible=" + getVisibility());
		if (height < 0)
			return;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
				.getLayoutParams();
		lp.bottomMargin = height;
		mContentView.setLayoutParams(lp);
	}

	public int getBottomMargin() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
				.getLayoutParams();
		return lp.bottomMargin;
	}

	/**
	 * normal status
	 */
	public void normal() {
		// mHintView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
	}

	/**
	 * loading status
	 */
	public void loading() {
		mHintView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
	}

	/**
	 * hide footer when disable pull load more
	 */
	public void hide() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
				.getLayoutParams();
		lp.height = 0;
		mContentView.setLayoutParams(lp);
	}

	/**
	 * show footer
	 */
	public void show() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
				.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		mContentView.setLayoutParams(lp);
	}

	private void initView(Context context) {
		mContext = context;
		LinearLayout moreView = (LinearLayout) LayoutInflater.from(mContext)
				.inflate(R.layout.xrefreshview_footer, null);
		addView(moreView);
		moreView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		mContentView = moreView.findViewById(R.id.xrefreshview_footer_content);
		mProgressBar = moreView
				.findViewById(R.id.xrefreshview_footer_progressbar);
		mHintView = (TextView) moreView
				.findViewById(R.id.xrefreshview_footer_hint_textview);
	}
}
