package com.andview.refreshview;

import android.annotation.SuppressLint;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

import com.andview.refreshview.listener.OnBottomLoadMoreTime;
import com.andview.refreshview.listener.OnTopRefreshTime;

public class XRefreshContentView implements OnScrollListener, OnTopRefreshTime,
		OnBottomLoadMoreTime {
	private View child;
	private XRefreshViewType childType = XRefreshViewType.NONE;
	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;
	private OnTopRefreshTime mTopRefreshTime;
	private OnBottomLoadMoreTime mBottomLoadMoreTime;

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

	public void scrollToTop() {
		child.scrollTo(0, 0);
	}

	public void setScrollListener() {
		if (child instanceof AbsListView) {
			AbsListView absListView = (AbsListView) child;
			absListView.setOnScrollListener(this);
		}
	}

	public boolean isTop() {
		if (mTopRefreshTime != null) {
			return mTopRefreshTime.isTop();
		}
		return !canChildScrollUp();
	}

	public boolean isBottom() {
		if (mBottomLoadMoreTime != null) {
			return mBottomLoadMoreTime.isBottom();
		}
		return !canChildScrollDown();
	}

	/**
	 * 设置顶部监听
	 * 
	 * @param topListener
	 */
	public void setOnTopRefreshTime(OnTopRefreshTime topRefreshTime) {
		this.mTopRefreshTime = topRefreshTime;
	}

	/**
	 * 设置底部监听
	 * 
	 * @param mRefreshBase
	 */
	public void setOnBottomLoadMoreTime(OnBottomLoadMoreTime bottomLoadMoreTime) {
		this.mBottomLoadMoreTime = bottomLoadMoreTime;
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

	/**
	 * @return Whether it is possible for the child view of this layout to
	 *         scroll up. Override this if the child view is a custom view.
	 */
	@SuppressLint("NewApi")
	public boolean canChildScrollUp() {
		if (android.os.Build.VERSION.SDK_INT < 14) {
			if (child instanceof AbsListView) {
				final AbsListView absListView = (AbsListView) child;
				return absListView.getChildCount() > 0
						&& (absListView.getFirstVisiblePosition() > 0 || absListView
								.getChildAt(0).getTop() < absListView
								.getPaddingTop());
			} else {
				return child.getScrollY() > 0;
			}
		} else {
			return child.canScrollVertically(-1);
		}
	}

	public boolean canChildScrollDown() {
		// 现阶段，为了兼容android4.0以下的版本，你自己得设置view到达底部的时机
		if (android.os.Build.VERSION.SDK_INT < 14) {
			if (child instanceof AbsListView) {
				AbsListView absListView = (AbsListView) child;
				return absListView.getLastVisiblePosition() != mTotalItemCount - 1;
			} else if (child instanceof WebView) {
				WebView webview = (WebView) child;
				return webview.getContentHeight() * webview.getScale() != webview
						.getHeight() + webview.getScrollY();
			} else if (child instanceof ScrollView) {
				ScrollView scrollView = (ScrollView) child;
				View childView = scrollView.getChildAt(0);
				if (childView != null) {
					return scrollView.getScrollY() != childView.getHeight()
							- scrollView.getHeight();
				}

			}
		} else {
			return ViewCompat.canScrollVertically(child, 1);
		}
		return true;
	}
}
