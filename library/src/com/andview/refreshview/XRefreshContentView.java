package com.andview.refreshview;

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
	@SuppressWarnings("unused")
	private XRefreshViewType childType = XRefreshViewType.NONE;
	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;
	private OnTopRefreshTime mTopRefreshTime;
	private OnBottomLoadMoreTime mBottomLoadMoreTime;
	private XRefreshView mContainer;
	private OnScrollListener mScrollListener;

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

	public void setContainer(XRefreshView container) {
		mContainer = container;
	}

	public void scrollToTop() {
		if (child instanceof AbsListView) {
			AbsListView absListView = (AbsListView) child;
			absListView.setSelection(0);
		} else {
			child.scrollTo(0, 0);
		}
	}

	public void setScrollListener() {
		if (child instanceof AbsListView) {
			AbsListView absListView = (AbsListView) child;
			absListView.setOnScrollListener(this);
		}
	}

	public void setOnScrollListener(OnScrollListener listener) {
		mScrollListener = listener;
	}

	public boolean isTop() {
		if (mTopRefreshTime != null) {
			return mTopRefreshTime.isTop();
		}
		return hasChildOnTop();
	}

	public boolean isBottom() {
		if (mBottomLoadMoreTime != null) {
			return mBottomLoadMoreTime.isBottom();
		}
		return hasChildOnBottom();
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
		if (mContainer!=null&&scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& mTotalItemCount - 1 == view.getLastVisiblePosition()) {
			mContainer.invoketLoadMore();
		}
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	}

	public int getTotalItemCount() {
		return mTotalItemCount;
	}

	public boolean hasChildOnTop() {
		return !canChildPullDown();
	}

	public boolean hasChildOnBottom() {
		return !canChildPullUp();
	}

	/**
	 * @return Whether it is possible for the child view of this layout to
	 *         scroll up. Override this if the child view is a custom view.
	 */
	public boolean canChildPullDown() {
		if (child instanceof AbsListView) {
			final AbsListView absListView = (AbsListView) child;
			return canScrollVertically(child, -1)
					|| absListView.getChildCount() > 0
					&& (absListView.getFirstVisiblePosition() > 0 || absListView
							.getChildAt(0).getTop() < absListView
							.getPaddingTop());
		} else {
			return canScrollVertically(child, -1) || child.getScrollY() > 0;
		}
	}

	public boolean canChildPullUp() {
		if (child instanceof AbsListView) {
			AbsListView absListView = (AbsListView) child;
			return canScrollVertically(child, 1)
					|| absListView.getLastVisiblePosition() != mTotalItemCount - 1;
		} else if (child instanceof WebView) {
			WebView webview = (WebView) child;
			return canScrollVertically(child, 1)
					|| webview.getContentHeight() * webview.getScale() != webview
							.getHeight() + webview.getScrollY();
		} else if (child instanceof ScrollView) {
			ScrollView scrollView = (ScrollView) child;
			View childView = scrollView.getChildAt(0);
			if (childView != null) {
				return canScrollVertically(child, 1)
						|| scrollView.getScrollY() != childView.getHeight()
								- scrollView.getHeight();
			}
		} else {
			return canScrollVertically(child, 1);
		}
		return true;
	}

	/**
	 * 用来判断view在竖直方向上能不能向上或者向下滑动
	 * 
	 * @param view
	 *            v
	 * @param direction
	 *            方向 负数代表向上滑动 ，正数则反之
	 * @return
	 */
	public boolean canScrollVertically(View view, int direction) {
		return ViewCompat.canScrollVertically(view, direction);
	}
}
