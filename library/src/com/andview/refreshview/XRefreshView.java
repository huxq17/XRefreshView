package com.andview.refreshview;

import java.util.Calendar;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
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

import com.lidroid.xutils.util.LogUtils;

public class XRefreshView extends LinearLayout implements OnScrollListener,
		XRefreshViewBase {
	private XRefreshViewType childType = XRefreshViewType.NONE;
	private View child;
	// -- header view
	private XRefreshViewHeader mHeaderView;

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

	private XRefreshViewBase mRefreshBase;
	private boolean isHeightMatchParent = true;
	private boolean isWidthMatchParent = true;
	/**
	 * 自定义header布局
	 */
	private XRefreshHeaderViewBase mCustomHeaderView;
	/**
	 * 自定义footer布局
	 */
	private XRefreshFooterViewBase mCustomFooterView;

	private static boolean animaDoing = false;
	private AnimaListener animaListener;
	/**
	 * 默认不自动刷新
	 */
	private boolean autoRefresh = false;

	public XRefreshView(Context context) {
		this(context, null);
	}

	public XRefreshView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClickable(true);
		setLongClickable(true);
		animaListener = new AnimaListener();
		initWithContext(context, attrs);
		setOrientation(VERTICAL);
	}

	/**
	 * 设置自定义的刷新规则
	 * 
	 * @param mRefreshBase
	 */
	public void setRefreshBase(XRefreshViewBase mRefreshBase) {
		this.mRefreshBase = mRefreshBase;
	}

	@Override
	protected void onFinishInflate() {
		child = XRefreshView.this.getChildAt(1);
		LinearLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
		if (isHeightMatchParent) {
			lp.height = LayoutParams.MATCH_PARENT;
		}
		if (isWidthMatchParent) {
			lp.height = LayoutParams.MATCH_PARENT;
		}
		// 默认设置宽高为match_parent
		child.setLayoutParams(lp);
		super.onFinishInflate();
	}

	private void initWithContext(Context context, AttributeSet attrs) {
		LogUtils.i("initWithContext");
		// 根据属性设置参数
		if (attrs != null) {
			TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.XRefreshView, 0, 0);
			try {
				isHeightMatchParent = a.getBoolean(
						R.styleable.XRefreshView_isHeightMatchParent, true);
				isWidthMatchParent = a.getBoolean(
						R.styleable.XRefreshView_isHeightMatchParent, true);
				autoRefresh = a.getBoolean(R.styleable.XRefreshView_autoRefresh, false);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				a.recycle();
			}
		}
		mHeaderView = new XRefreshViewHeader(context);

		addView(mHeaderView);

		mFooterView = new XRefreshViewFooter(context);
		this.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						mHeaderViewHeight = mHeaderView
								.getHeaderContentHeight();
						LogUtils.i("onGlobalLayout mHeaderViewHeight="
								+ mHeaderViewHeight);
						setScrollListener();
						if (mEnablePullLoad) {
							Log.i("CustomView", "add footView");
							addView(mFooterView);
						}
						// 移除视图树监听器
						removeViewTreeObserver(this);
						if (autoRefresh) {
							startRefresh();
						}
					}
				});
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void removeViewTreeObserver(OnGlobalLayoutListener listener) {
		if (Build.VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
			getViewTreeObserver().removeGlobalOnLayoutListener(listener);
		} else {
			getViewTreeObserver().removeOnGlobalLayoutListener(listener);
		}
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
		int childCount = getChildCount();
		int top = getPaddingTop();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			child.layout(0, top, child.getMeasuredWidth(),
					child.getMeasuredHeight() + top);
			top += child.getMeasuredHeight();
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
		if (mPullLoading || mPullRefreshing || animaDoing) {
			return super.onInterceptTouchEvent(ev);
		}
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
	public void setRefreshViewType(XRefreshViewType type) {
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
			mFooterView.setState(XRefreshViewState.STATE_LOADING);
		}
	}

	public void setmCustomHeaderView(XRefreshHeaderViewBase mCustomHeaderView) {
		this.mCustomHeaderView = mCustomHeaderView;
	}

	public void setmCustomFooterView(XRefreshFooterViewBase mCustomFooterView) {
		this.mCustomFooterView = mCustomFooterView;
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
		float deltaY = 0;
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			if (isTop() && (deltaY > 0 || headY > 0)) {
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
			if (isTop() && headY > 0) {
				// invoke refresh
				if (!mPullRefreshing && mEnablePullRefresh
						&& headY > mHeaderViewHeight) {
					mPullRefreshing = true;
					mHeaderView.setState(XRefreshViewState.STATE_REFRESHING);
					if (mRefreshViewListener != null) {
						mRefreshViewListener.onRefresh();
					}
				}
				resetHeaderHeight();
			} else if (isBottom()) {
				int footHeight = mFooterView.getMeasuredHeight();
				if (!mPullLoading && mEnablePullLoad) {
					moveChildAndAddedView(mFooterView, mChildY - footHeight,
							mFootY - footHeight, SCROLL_DURATION);
					startLoadMore();
				}
			}
			mLastY = -1; // reset
			// mChildY = -1;
			// mFootY = -1;
			break;
		}
		return super.onTouchEvent(ev);
	}

	public void moveChildAndAddedView(View addView, float childY, float addY,
			int during, AnimatorListener... listener) {
		// 属性动画移动
		ObjectAnimator y = ObjectAnimator.ofFloat(child, "y", child.getY(),
				childY);
		ObjectAnimator y2 = ObjectAnimator.ofFloat(addView, "y",
				addView.getY(), addY);

		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(y, y2);
		animatorSet.setDuration(during);
		if (listener.length > 0)
			animatorSet.addListener(listener[0]);
		animatorSet.start();
	}

	float childY;
	public float headY;

	/**
	 * 如果第二个可变参数不为空，则代表是自动刷新
	 * 
	 * @param delta
	 * @param during
	 */
	private void updateHeaderHeight(float delta, int... during) {
		if (mChildY == -1 || mHeadY == -1) {
			mChildY = child.getY();
			mHeadY = mHeaderView.getY();
		}
		childY = child.getY() + delta;
		headY = mHeaderView.getY() + delta;
		if (during != null && during.length > 0) {
			mHeaderView.setState(XRefreshViewState.STATE_REFRESHING);
			moveChildAndAddedView(mHeaderView, childY, headY, during[0]);
		} else {
			moveChildAndAddedView(mHeaderView, childY, headY, 0);
			if (mEnablePullRefresh && !mPullRefreshing) {
				if (headY > mHeaderViewHeight) {
					mHeaderView.setState(XRefreshViewState.STATE_READY);
				} else {
					mHeaderView.setState(XRefreshViewState.STATE_NORMAL);
				}
			}
		}
	}

	private void updateFooterHeight(float delta) {
		if (mChildY == -1 || mFootY == -1) {
			mChildY = child.getY();
			mFootY = mFooterView.getY();
		}
		float childY = child.getY() - delta;
		float footY = mFooterView.getY() - delta;
		LogUtils.i("mFootY=" + mFootY + ";child=" + footY);
		moveChildAndAddedView(mFooterView, childY, footY, 0);
		// mFooterView.setState(XRefreshViewState.STATE_LOADING);
	}

	/**
	 * 设置是否自动刷新，默认不自动刷新
	 * 
	 * @param autoRefresh
	 *            true则自动刷新
	 */
	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
		setRefreshTime();
	}

	public void startRefresh() {
		mPullRefreshing = true;
		if (mRefreshViewListener != null) {
			mRefreshViewListener.onRefresh();
		}
		updateHeaderHeight(mHeaderViewHeight, SCROLL_DURATION);
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
		int headHeight = mHeaderViewHeight;
		if (mPullRefreshing) {
			moveChildAndAddedView(mHeaderView, mChildY + headHeight, mHeadY
					+ headHeight, SCROLL_DURATION);
		} else {
			moveChildAndAddedView(mHeaderView, mChildY, -mHeadY,
					SCROLL_DURATION, animaListener);
		}
	}

	/**
	 * enable or disable pull down refresh feature.
	 * 
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
	}

	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		LogUtils.i("stopRefresh mPullRefreshing=" + mPullRefreshing);
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();
			headY = 0;
			lastRefreshTime = Calendar.getInstance().getTimeInMillis();
		}
	}

	private long lastRefreshTime = -1;

	/**
	 * 恢复上次刷新的时间
	 * 
	 * @param lastRefreshTime
	 */
	public void restoreLastRefreshTime(long lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}

	/**
	 * 在停止刷新的时候调用，记录这次刷新的时间，用于下次刷新的时候显示
	 * 
	 * @return
	 */
	public long getLastRefreshTime() {
		return lastRefreshTime;
	}

	private void setRefreshTime() {
		if (lastRefreshTime <= 0) {
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
			moveChildAndAddedView(mFooterView, mChildY, mFootY, 0,
					animaListener);
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

	public class AnimaListener implements AnimatorListener {

		@Override
		public void onAnimationStart(Animator animation) {
			animaDoing = true;
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			animaDoing = false;
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			animaDoing = false;
		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}

	}
}