package com.andview.refreshview;

import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.andview.refreshview.listener.OnBottomLoadMoreTime;
import com.andview.refreshview.listener.OnTopRefreshTime;
import com.lidroid.xutils.util.LogUtils;

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
		if (mTopRefreshTime != null) {
			return mTopRefreshTime.isTop();
		}
		return !canChildScrollUp();
	}

	public boolean isBottom() {
		if (mBottomLoadMoreTime != null) {
			return mBottomLoadMoreTime.isBottom();
		}
		if (Build.VERSION.SDK_INT < 14) {
			// 现阶段，为了兼容android4.0以下的版本，你自己得设置view到达底部的时机
		} else {
			if (!canChildScrollDown()) {
				LogUtils.i("isBottom");
				return true;
			}
		}
		return false;
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
	public boolean canChildScrollUp() {
		if (android.os.Build.VERSION.SDK_INT < 14) {
			LogUtils.i("child.getScrollY()=" + child.getScrollY()
					+ "ViewCompat.canScrollVertically(child, -1)="
					+ ViewCompat.canScrollVertically(child, -1));
			return ViewCompat.canScrollVertically(child, -1)
					|| child.getScrollY() > 0;
		} else {
			return ViewCompat.canScrollVertically(child, -1);
		}
	}

	public boolean canChildScrollDown() {
		if (android.os.Build.VERSION.SDK_INT < 14) {
			//
		} else {
			return ViewCompat.canScrollVertically(child, 1);
		}
		return true;
	}
}
