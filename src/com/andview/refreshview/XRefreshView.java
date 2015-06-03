package com.andview.refreshview;

import java.util.Calendar;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.lidroid.xutils.util.LogUtils;

public class XRefreshView extends LinearLayout implements OnScrollListener,
		RefreshBase {
	private RefreshViewType childType = RefreshViewType.NONE;
	private View child;
	// -- header view
	private XRefreshViewHeader mHeaderView;
	// header view content, use it to calculate the Header's height. And hide it
	// when disable pull refresh.
	private RelativeLayout mHeaderViewContent;
	private int mHeaderViewHeight; // header view's height
	/**
	 * 最初的滚动位置.第一次布局时滚动header的高度的距离
	 */
	protected int mInitScrollY = 0;
	private float mLastY = -1; // save event y
	private boolean mEnablePullRefresh = true;
	public boolean mPullRefreshing = false; // is refreashing.
	private final static float OFFSET_RADIO = 1.8f; // support iOS like pull
	private OnScrollListener mScrollListener; // user's scroll listener

	private final static int SCROLL_DURATION = 400; // scroll back duration
	// the interface to trigger refresh and load more.
	private XRefreshViewListener mRefreshViewListener;
	// -- footer view
	private XRefreshViewFooter mFooterView;
	private boolean mEnablePullLoad;
	public boolean mPullLoading;
	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;
	/**
	 * 在开始上拉加载更多的时候，记录下childView一开始的Y轴坐标
	 */
	private float mChildY = -1;
	/**
	 * 在开始上拉加载更多的时候，记录下FootView一开始的Y轴坐标
	 */
	private float mFootY = -1;
	/**
	 * 在开始上拉加载更多的时候，记录下HeadView一开始的Y轴坐标
	 */
	private float mHeadY = -1;

	private RefreshBase mRefreshBase;
	private boolean isHeightMatchParent = true;
	private boolean isWidthMatchParent = true;

	public XRefreshView(Context context) {
		this(context, null);
	}

	public XRefreshView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClickable(true);
		setLongClickable(true);
		initWithContext(context, attrs);
		setOrientation(VERTICAL);
	}

	/**
	 * 设置自定义的刷新规则
	 * 
	 * @param mRefreshBase
	 */
	public void setRefreshBase(RefreshBase mRefreshBase) {
		this.mRefreshBase = mRefreshBase;
	}

	private void initWithContext(Context context, AttributeSet attrs) {

		// 根据属性设置参数
		if (attrs != null) {
			TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.XRefreshView, 0, 0);
			try {
				isHeightMatchParent = a.getBoolean(
						R.styleable.XRefreshView_isHeightMatchParent, true);
				isWidthMatchParent = a.getBoolean(
						R.styleable.XRefreshView_isHeightMatchParent, true);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				a.recycle();
			}
		}
		mHeaderView = new XRefreshViewHeader(context);
		mHeaderViewContent = (RelativeLayout) mHeaderView
				.findViewById(R.id.xrefreshview_header_content);
		addView(mHeaderView);

		mFooterView = new XRefreshViewFooter(context);
		this.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@SuppressWarnings("deprecation")
					@SuppressLint("NewApi")
					@Override
					public void onGlobalLayout() {
						mHeaderViewHeight = mHeaderViewContent
								.getMeasuredHeight();

						child = XRefreshView.this.getChildAt(1);
						LinearLayout.LayoutParams lp = (LayoutParams) child
								.getLayoutParams();
						if (isHeightMatchParent) {
							lp.height = LayoutParams.MATCH_PARENT;
						}
						if (isWidthMatchParent) {
							lp.height = LayoutParams.MATCH_PARENT;
						}
						// 默认设置宽高为match_parent
						child.setLayoutParams(lp);
						// 移除视图树监听器
						if (Build.VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
							getViewTreeObserver().removeGlobalOnLayoutListener(
									this);
						} else {
							getViewTreeObserver().removeOnGlobalLayoutListener(
									this);
						}
						setScrollListener();
						if (mEnablePullLoad) {
							Log.i("CustomView", "add footView");
							addView(mFooterView);
						}
					}
				});
	}

	protected void setScrollListener() {
		switch (childType) {
		case ABSLISTVIEW:
			AbsListView absListView = (AbsListView) child;
			absListView.setOnScrollListener(this);
			break;

		default:
			break;
		}
	}

	/*
	 * 丈量视图的宽、高。宽度为用户设置的宽度，高度则为header, content view, footer这三个子控件的高度只和。
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mHeadY > 0) {
			return;
		}
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int childCount = getChildCount();
		int finalHeight = 0;
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			measureChild(child, widthMeasureSpec, heightMeasureSpec);
			finalHeight += child.getMeasuredHeight();
		}
		setMeasuredDimension(width, finalHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mHeadY > 0) {
			return;
		}
		// 计算初始化滑动的y轴距离
		mInitScrollY = mHeaderView.getMeasuredHeight() + getPaddingTop();
		// 滑动到header view高度的位置, 从而达到隐藏header view的效果
		scrollTo(0, mInitScrollY);
	}

	/*
	 * 在适当的时候拦截触摸事件，这里指的适当的时候是当mContentView滑动到顶部，并且是下拉时拦截触摸事件，否则不拦截，交给其child
	 * view 来处理。
	 * 
	 * @see
	 * android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		/*
		 * This method JUST determines whether we want to intercept the motion.
		 * If we return true, onTouchEvent will be called and we do the actual
		 * scrolling there.
		 */
		final int action = MotionEventCompat.getActionMasked(ev);
		// Always handle the case of the touch gesture being complete.
		if (action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_UP) {
			// Do not intercept touch event, let the child handle it
			return false;
		}
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}
		switch (action) {

		case MotionEvent.ACTION_DOWN:
			mLastY = (int) ev.getRawY();
			break;

		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			// 如果拉到了顶部, 并且是下拉,则拦截触摸事件,从而转到onTouchEvent来处理下拉刷新事件
			if (isTop() && deltaY > 0) {
				setRefreshTime();
				return true;
			} else if (isBottom() && deltaY < 0) {
				return true;
			}
			break;

		}
		return super.onInterceptTouchEvent(ev);
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

			break;
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

			break;
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
	 * 在初始化的时候调用
	 * 
	 * @param type
	 */
	public void setRefreshViewType(RefreshViewType type) {
		this.childType = type;
	}

	/**
	 * enable or disable pull up load more feature.
	 * 
	 * @param enable
	 */
	public void setPullLoadEnable(boolean enable) {
		LogUtils.i("setPullLoadEnable");
		mEnablePullLoad = enable;
		if (!mEnablePullLoad) {
			mFooterView.hide();
			mFooterView.setOnClickListener(null);
		} else {
			mPullLoading = false;
			mFooterView.show();
			mFooterView.setState(XRefreshViewFooter.STATE_NORMAL);
			// both "pull up" and "click" will invoke load more.
			mFooterView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startLoadMore();
				}
			});
		}
	}

	private void startLoadMore() {
		mPullLoading = true;
		if (mRefreshViewListener != null) {
			mRefreshViewListener.onLoadMore();
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			if (isTop() && (deltaY > 0 || mHeaderView.getVisiableHeight() > 0)) {
				if (!mPullRefreshing) {
					updateHeaderHeight(deltaY / OFFSET_RADIO);
					// invokeOnScrolling();
				}
			} else if (isBottom() && deltaY < 0 && mEnablePullLoad) {
				if (!mPullLoading) {
					updateFooterHeight(-deltaY / OFFSET_RADIO);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			mLastY = -1; // reset
			if (isTop()) {
				// invoke refresh
				if (!mPullRefreshing && mEnablePullRefresh
						&& headY > mHeaderViewHeight) {
					mPullRefreshing = true;
					mHeaderView.setState(XRefreshViewHeader.STATE_REFRESHING);
					if (mRefreshViewListener != null) {
						mRefreshViewListener.onRefresh();
					}
				}
				resetHeaderHeight();
			} else if (isBottom()) {
				int footHeight = mFooterView.height;
				if (!mPullLoading && mEnablePullLoad) {
					moveChildAndAddedView(mFooterView, mChildY - footHeight,
							mFootY - footHeight, SCROLL_DURATION);
					startLoadMore();
				}
			}
			// mChildY = -1;
			// mFootY = -1;
			break;
		}
		return super.onTouchEvent(ev);
	}

	public void moveChildAndAddedView(View addView, float childY, float addY,
			int during) {
		// 属性动画移动
		ObjectAnimator y = ObjectAnimator.ofFloat(child, "y", child.getY(),
				childY);
		ObjectAnimator y2 = ObjectAnimator.ofFloat(addView, "y",
				addView.getY(), addY);

		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(y, y2);
		animatorSet.setDuration(during);
		animatorSet.start();
	}

	float childY;
	public float headY;

	private void updateHeaderHeight(float delta) {
		if (mChildY == -1 || mHeadY == -1) {
			mChildY = child.getY();
			mHeadY = mHeaderView.getY();
		}
		childY = child.getY() + delta;
		headY = mHeaderView.getY() + delta;
		moveChildAndAddedView(mHeaderView, childY, headY, 0);
		if (mEnablePullRefresh && !mPullRefreshing) {
			if (headY > mHeaderViewHeight) {
				mHeaderView.setState(XRefreshViewHeader.STATE_READY);
			} else {
				mHeaderView.setState(XRefreshViewHeader.STATE_NORMAL);
			}
		}
		// if (child instanceof AbsListView) {
		// AbsListView listView = (AbsListView) child;
		// listView.setSelection(0); // scroll to top each time
		// }
	}

	private void updateFooterHeight(float delta) {
		if (mChildY == -1 || mFootY == -1) {
			mChildY = child.getY();
			mFootY = mFooterView.getY();
		}
		float childY = child.getY() - delta;
		float footY = mFooterView.getY() - delta;
		moveChildAndAddedView(mFooterView, childY, footY, 0);
		mFooterView.setState(XRefreshViewFooter.STATE_LOADING);
	}

	/**
	 * reset header view's height.
	 */
	private void resetHeaderHeight() {
		int height = (int) headY;
		if (height == 0) // not visible.
			return;
		// refreshing and header isn't shown fully. do nothing.
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int headHeight = mHeaderView.height;
		if (mPullRefreshing) {
			moveChildAndAddedView(mHeaderView, mChildY + headHeight, mHeadY
					+ headHeight, SCROLL_DURATION);

		} else {
			moveChildAndAddedView(mHeaderView, mChildY, -mHeadY,
					SCROLL_DURATION);
		}
	}

	/**
	 * enable or disable pull down refresh feature.
	 * 
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable, hide the content
			mHeaderViewContent.setVisibility(View.INVISIBLE);
		} else {
			mHeaderViewContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		LogUtils.i("stopRefresh");
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();
			headY = 0;
			lastRefreshTime = Calendar.getInstance().getTimeInMillis();
		}
	}

	private long lastRefreshTime = -1;

	private void setRefreshTime() {
		if (lastRefreshTime < 0) {
			return;
		}
		Calendar mCalendar = Calendar.getInstance();
		long refreshTime = mCalendar.getTimeInMillis();
		long howLong = refreshTime - lastRefreshTime;
		int minutes = (int) (howLong / 1000 / 60);
		String refreshTimeText = null;
		Resources resources = getContext().getResources();
		if (minutes < 1) {
			refreshTimeText = resources
					.getString(R.string.xrefreshview_refresh_justnow);
		} else if (minutes < 60) {
			refreshTimeText = resources
					.getString(R.string.xrefreshview_refresh_minutes_ago);
			refreshTimeText = format(refreshTimeText, minutes);
		} else if (minutes < 60 * 24) {
			refreshTimeText = resources
					.getString(R.string.xrefreshview_refresh_hours_ago);
			refreshTimeText = format(refreshTimeText, minutes / 60);
		} else {
			refreshTimeText = resources
					.getString(R.string.xrefreshview_refresh_days_ago);
			refreshTimeText = format(refreshTimeText, minutes / 60 / 24);
		}
		mHeaderView.setRefreshTime(refreshTimeText);
	}

	/**
	 * 格式化字符串
	 * 
	 * @param format
	 * @param args
	 */
	public String format(String format, int args) {
		return String.format(format, args);
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			// 隐藏footView
			moveChildAndAddedView(mFooterView, mChildY, mFootY, 0);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mTotalItemCount = totalItemCount;
		// send to user's listener
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	}

	/**
	 * you can listen ListView.OnScrollListener or this one. it will invoke
	 * onXScrolling when header/footer scroll back.
	 */
	public interface OnXScrollListener extends OnScrollListener {
		public void onXScrolling(View view);
	}

	public void setXRefreshViewListener(XRefreshViewListener l) {
		mRefreshViewListener = l;
	}

	/**
	 * implements this interface to get refresh/load more event.
	 */
	public interface XRefreshViewListener {
		public void onRefresh();

		public void onLoadMore();
	}
}