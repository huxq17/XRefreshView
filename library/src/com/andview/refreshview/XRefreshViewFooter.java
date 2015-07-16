package com.andview.refreshview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.andview.refreshview.callback.IFooterCallBack;

public class XRefreshViewFooter extends LinearLayout implements IFooterCallBack {
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

	@Override
	public void onStateRefreshing() {
		mHintView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public void onStateEnd() {
		mHintView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
	}

	public void hide() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
				.getLayoutParams();
		lp.height = 0;
		mContentView.setLayoutParams(lp);
	}

	public void show() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
				.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		mContentView.setLayoutParams(lp);
	}

	private void initView(Context context) {
		mContext = context;
		RelativeLayout moreView = (RelativeLayout) LayoutInflater
				.from(mContext).inflate(R.layout.xrefreshview_footer, null);
		addView(moreView);

		mContentView = moreView.findViewById(R.id.xrefreshview_footer_content);
		mProgressBar = moreView
				.findViewById(R.id.xrefreshview_footer_progressbar);
		mHintView = (TextView) moreView
				.findViewById(R.id.xrefreshview_footer_hint_textview);
	}

}
