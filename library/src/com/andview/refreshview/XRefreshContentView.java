package com.andview.refreshview;

import com.andview.refreshview.base.XRefreshContentViewBase;

import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout.LayoutParams;

public class XRefreshContentView implements OnScrollListener,
		XRefreshContentViewBase {
	private View child;
	private XRefreshViewType childType = XRefreshViewType.NONE;
	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;
	private XRefreshContentViewBase mRefreshBase;

	public void setContentViewLayoutParams(boolean isHeightMatchParent,
			boolean isWidthMatchParent) {
		LinearLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
		if (isHeightMatchParent) {
			lp.height = LayoutParams.MATCH_PARENT;
		}
		if (isWidthMatchParent) {
			lp.height = LayoutParams.MATCH_PARENT;
		}
		// 默认设置宽高为match_parent
		child.setLayoutParams(lp);
	}

	public View setContentView(View child) {
		this.child = child;
		return this.child;
	}

	public View getContentView() {
		return child;
	}

	public void setScrollListener() {
		switch (childType) {
		case ABSLISTVIEW:
			AbsListView absListView = (AbsListView) child;
			absListView.setOnScrollListener(this);
			break;

		default:
			break;
		}
	}

	public boolean isTop() {
		if (mRefreshBase != null) {
			return mRefreshBase.isTop();
		}
		switch (childType) {
		case ABSLISTVIEW:
			AbsListView absListView = (AbsListView) child;
			return absListView.getFirstVisiblePosition() == 0;
		case WEBVIEW:
			WebView webView = (WebView) child;
			return webView.getScrollY() == 0;
		case SCROLLVIEW:

			break;
		case NOSCROLLVIEW:

			break;
		case NONE:

			break;

		default:
			break;
		}
		return false;
	}

	public boolean isBottom() {
		if (mRefreshBase != null) {
			return mRefreshBase.isBottom();
		}
		switch (childType) {
		case ABSLISTVIEW:
			AbsListView absListView = (AbsListView) child;
			return absListView.getLastVisiblePosition() == mTotalItemCount - 1;
		case WEBVIEW:
			WebView webView = (WebView) child;
			return webView.getContentHeight()*webView.getScale()==(webView.getHeight()+webView.getScrollY());
		case SCROLLVIEW:

			break;
		case NOSCROLLVIEW:

			break;
		case NONE:

			break;

		default:
			break;
		}
		return false;
	}

	/**
	 * 设置自定义的刷新规则
	 * 
	 * @param mRefreshBase
	 */
	public void setRefreshBase(XRefreshContentViewBase mRefreshBase) {
		this.mRefreshBase = mRefreshBase;
	}

	public void setRefreshViewType(XRefreshViewType type) {
		this.childType = type;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mTotalItemCount = totalItemCount;
	}

	public int getTotalItemCount() {
		return mTotalItemCount;
	}
}
