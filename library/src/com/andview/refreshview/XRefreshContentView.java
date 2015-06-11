package com.andview.refreshview;

import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.andview.refreshview.listener.OnBottomListener;
import com.andview.refreshview.listener.OnTopListener;
import com.lidroid.xutils.util.LogUtils;

public class XRefreshContentView implements OnScrollListener,
		OnTopListener,OnBottomListener {
	private View child;
	private XRefreshViewType childType = XRefreshViewType.NONE;
	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;
	private OnTopListener mTopListener;
	private OnBottomListener mBottomListener;

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
		if (mTopListener != null) {
			return mTopListener.isTop();
		}
		if (Build.VERSION.SDK_INT < 14) {
			if (child.getScrollY() == 0) {
				return true;
			}
		} else {
			if (!ViewCompat.canScrollVertically(child, -1)) {
				return true;
			}
		}
		// switch (childType) {
		// case ABSLISTVIEW:
		// AbsListView absListView = (AbsListView) child;
		// return absListView.getFirstVisiblePosition() == 0;
		// case WEBVIEW:
		// WebView webView = (WebView) child;
		// return webView.getScrollY() == 0;
		// case SCROLLVIEW:
		//
		// break;
		// case NOSCROLLVIEW:
		//
		// break;
		// case NONE:
		//
		// break;
		//
		// default:
		// break;
		// }
		return false;
	}

	public boolean isBottom() {
		if (mBottomListener != null) {
			return mBottomListener.isBottom();
		}
		if (Build.VERSION.SDK_INT < 14) {
			if (child.getScrollY() == 0) {
				return true;
			}
		} else {
			if (!ViewCompat.canScrollVertically(child, 1)) {
				LogUtils.i("isBottom");
				return true;
			}
		}
		switch (childType) {
		case ABSLISTVIEW:
			AbsListView absListView = (AbsListView) child;
			return absListView.getLastVisiblePosition() == mTotalItemCount - 1;
		case WEBVIEW:
			WebView webView = (WebView) child;
			return webView.getContentHeight() * webView.getScale() == (webView
					.getHeight() + webView.getScrollY());
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
	 * 设置顶部监听
	 * 
	 * @param topListener
	 */
	public void setTopListener(OnTopListener topListener) {
		this.mTopListener = topListener;
	}
	/**
	 * 设置底部监听
	 * 
	 * @param mRefreshBase
	 */
	public void setBottomListener(OnBottomListener bottomListener) {
		this.mBottomListener = bottomListener;
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
