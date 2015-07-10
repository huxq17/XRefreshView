package com.andview.refreshview;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.andview.refreshview.base.XRefreshFooterViewBase;
import com.andview.refreshview.base.XRefreshHeaderViewBase;
import com.andview.refreshview.listener.OnBottomLoadMoreTime;
import com.andview.refreshview.listener.OnTopRefreshTime;
import com.andview.refreshview.utils.LogUtils;

public class XRefreshView extends LinearLayout {
	private View mChild;
	// -- header view
	private XRefreshViewHeader mHeaderView;

	private int mHeaderViewHeight; // header view's height
	/**
	 * 最初的滚动位置.第一次布局时滚动header的高度的距离
	 */
	protected int mInitScrollY = 0;
	private int mLastY = -1; // save event y
	private int mLastX = -1; // save event x
	private boolean mEnablePullRefresh = true;
	public boolean mPullRefreshing = false; // is refreashing.
	private final static float OFFSET_RADIO = 1.8f; // support iOS like pull

	private final static int SCROLL_DURATION = 400; // scroll back duration
	// the interface to trigger refresh and load more.
	private XRefreshViewListener mRefreshViewListener;
	// -- footer view
	private XRefreshViewFooter mFooterView;
	private boolean mEnablePullLoad;
	public boolean mPullLoading;
	/**
	 * 自定义header布局
	 */
	private XRefreshHeaderViewBase mCustomHeaderView;
	/**
	 * 自定义footer布局
	 */
	private XRefreshFooterViewBase mCustomFooterView;

	/**
	 * 默认不自动刷新
	 */
	private boolean autoRefresh = false;
	/**
	 * 默认自动加载更多
	 */
	private boolean autoLoadMore = true;
	private int mFootHeight;
	/**
	 * 被刷新的view
	 */
	private XRefreshContentView mContentView;
	private boolean isHeightMatchParent = true;
	private boolean isWidthMatchParent = true;
	private int mInitialMotionY;
	private int mTouchSlop;
	private XRefreshHolder mHolder;

	private MotionEvent mLastMoveEvent;
	private boolean mHasSendCancelEvent = false;
	private boolean mHasSendDownEvent = false;
	private Scroller mScroller;
	private boolean mMoveForHorizontal = false;
	private boolean isForHorizontalMove = false;
	private boolean mIsIntercept = false;

	public XRefreshView(Context context) {
		this(context, null);
	}

	public XRefreshView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClickable(true);
		setLongClickable(true);
		mContentView = new XRefreshContentView();
		mHolder = new XRefreshHolder();
		mScroller = new Scroller(getContext(), new LinearInterpolator());

		initWithContext(context, attrs);
		setOrientation(VERTICAL);
	}

	/**
	 * 设置顶部刷新时机
	 * 
	 * @param topListener
	 */
	public void setOnTopRefreshTime(OnTopRefreshTime topListener) {
		mContentView.setOnTopRefreshTime(topListener);
	}

	public void setOnBottomLoadMoreTime(OnBottomLoadMoreTime bottomListener) {
		mContentView.setOnBottomLoadMoreTime(bottomListener);
	}

	@Override
	protected void onFinishInflate() {
		mChild = mContentView.setContentView(XRefreshView.this.getChildAt(1));
		if (autoLoadMore) {
			mContentView.setContainer(this);
		}
		mContentView.setContentViewLayoutParams(isHeightMatchParent,
				isWidthMatchParent);
		super.onFinishInflate();
	}

	/**
	 * if need use for Horizontal move,pass true, or false
	 * 
	 * @param isDisableMoveForHorizontal
	 *            default false
	 */
	public void setMoveForHorizontal(boolean isForHorizontalMove) {
		this.isForHorizontalMove = isForHorizontalMove;
	}

	private void initWithContext(Context context, AttributeSet attrs) {
		LogUtils.d("initWithContext");

		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		// 根据属性设置参数
		if (attrs != null) {
			TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.XRefreshView, 0, 0);
			try {
				isHeightMatchParent = a.getBoolean(
						R.styleable.XRefreshView_isHeightMatchParent, true);
				isWidthMatchParent = a.getBoolean(
						R.styleable.XRefreshView_isHeightMatchParent, true);
				autoRefresh = a.getBoolean(
						R.styleable.XRefreshView_autoRefresh, false);
				autoLoadMore = a.getBoolean(
						R.styleable.XRefreshView_autoLoadMore, true);
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

						LogUtils.d("onGlobalLayout mHeaderViewHeight="
								+ mHeaderViewHeight);
						mContentView.setScrollListener();
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

	/*
	 * 丈量视图的宽、高。宽度为用户设置的宽度，高度则为header, content view, footer这三个子控件的高度之和。
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
		LogUtils.d("onLayout mHolder.mOffsetY=" + mHolder.mOffsetY);
		mFootHeight = mFooterView.getMeasuredHeight();
		int childCount = getChildCount();
		int top = getPaddingTop() + mHolder.mOffsetY;
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child == mHeaderView) {
				// 通过把headerview向上移动一个headerview高度的距离来达到隐藏headerview的效果
				child.layout(0, top - mHeaderViewHeight,
						child.getMeasuredWidth(), top);
			} else {
				child.layout(0, top, child.getMeasuredWidth(),
						child.getMeasuredHeight() + top);
				top += child.getMeasuredHeight();
			}
		}
	}

	private boolean isIntercepted;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		final int action = MotionEventCompat.getActionMasked(ev);
		int deltaY = 0;
		int deltaX = 0;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mHasSendCancelEvent = false;
			mHasSendDownEvent = false;
			mLastY = (int) ev.getRawY();
			mLastX = (int) ev.getRawX();
			mInitialMotionY = mLastY;

			if (!mScroller.isFinished() && !mPullRefreshing && !mPullLoading) {
				mScroller.forceFinished(true);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mPullLoading || mPullRefreshing || !isEnabled() || mIsIntercept) {
				return super.dispatchTouchEvent(ev);
			}
			mLastMoveEvent = ev;
			int currentY = (int) ev.getRawY();
			int currentX = (int) ev.getRawX();
			deltaY = currentY - mLastY;
			deltaX = currentX - mLastX;
			mLastY = currentY;
			// intercept the MotionEvent only when user is not scrolling
			if (!isIntercepted && (Math.abs(deltaY) < mTouchSlop)) {
				isIntercepted = true;
				return super.dispatchTouchEvent(ev);
			}
			if (isForHorizontalMove && !mMoveForHorizontal
					&& Math.abs(deltaX) > Math.abs(deltaY)) {
				if (mHolder.mOffsetY == 0) {
					mMoveForHorizontal = true;
				}
			}
			if (mMoveForHorizontal) {
				return super.dispatchTouchEvent(ev);
			}
			LogUtils.d("isTop=" + mContentView.isTop() + ";isBottom="
					+ mContentView.isBottom());
			deltaY = (int) (deltaY / OFFSET_RADIO);
			if (mContentView.isTop()
					&& (deltaY > 0 || (deltaY < 0 && mHolder
							.hasHeaderPullDown()))) {
				sendCancelEvent();
				updateHeaderHeight(currentY, deltaY);
			} else if (mContentView.isBottom()
					&& (deltaY < 0 || deltaY > 0 && mHolder.hasFooterPullUp())) {
				sendCancelEvent();
				updateFooterHeight(deltaY);
			} else if (mContentView.isTop() && !mHolder.hasHeaderPullDown()
					|| mContentView.isBottom() && !mHolder.hasFooterPullUp()) {
				if (Math.abs(deltaY) > 0)
					sendDownEvent();
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			// if (mHolder.mOffsetY != 0 && mRefreshViewListener != null
			// && !mPullRefreshing && !mPullLoading) {
			// mRefreshViewListener.onRelease(mHolder.mOffsetY);
			// }
			if (mHolder.hasHeaderPullDown()) {
				// invoke refresh
				if (mEnablePullRefresh && mHolder.mOffsetY > mHeaderViewHeight) {
					mPullRefreshing = true;
					mHeaderView.setState(XRefreshViewState.STATE_REFRESHING);
					if (mRefreshViewListener != null) {
						mRefreshViewListener.onRefresh();
					}
				}
				resetHeaderHeight();
			} else if (mHolder.hasFooterPullUp()) {
				if (mEnablePullLoad) {
					invoketLoadMore();
				} else {
					int offset = 0 - mHolder.mOffsetY;
					startScroll(offset, SCROLL_DURATION);
				}
			}
			mLastY = -1; // reset
			mInitialMotionY = 0;
			isIntercepted = true;
			mMoveForHorizontal = false;
			mIsIntercept = false;
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	public void invoketLoadMore() {
		if (!mPullLoading) {
			int offset = 0 - mHolder.mOffsetY - mFootHeight;
			startScroll(offset, SCROLL_DURATION);
			startLoadMore();
		}
	}

	/**
	 * if child need the touch event,pass true
	 */
	public void disallowInterceptTouchEvent(boolean isIntercept) {
		mIsIntercept = isIntercept;
	}

	private void sendCancelEvent() {
		if (!mHasSendCancelEvent) {
			setRefreshTime();
			mHasSendCancelEvent = true;
			mHasSendDownEvent = false;
			MotionEvent last = mLastMoveEvent;
			MotionEvent e = MotionEvent.obtain(
					last.getDownTime(),
					last.getEventTime()
							+ ViewConfiguration.getLongPressTimeout(),
					MotionEvent.ACTION_CANCEL, last.getX(), last.getY(),
					last.getMetaState());
			dispatchTouchEventSupper(e);
		}
	}

	private void sendDownEvent() {
		if (!mHasSendDownEvent) {
			LogUtils.d("sendDownEvent");
			mHasSendCancelEvent = false;
			mHasSendDownEvent = true;
			isIntercepted = false;
			final MotionEvent last = mLastMoveEvent;
			if (last == null)
				return;
			MotionEvent e = MotionEvent.obtain(last.getDownTime(),
					last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(),
					last.getY(), last.getMetaState());
			dispatchTouchEventSupper(e);
		}
	}

	public boolean dispatchTouchEventSupper(MotionEvent e) {
		return super.dispatchTouchEvent(e);
	}

	/**
	 * 在初始化的时候调用
	 * 
	 * @param type
	 */
	public void setRefreshViewType(XRefreshViewType type) {
		mContentView.setRefreshViewType(type);
	}

	/**
	 * enable or disable pull up load more feature.
	 * 
	 * @param enable
	 */
	public void setPullLoadEnable(boolean enable) {
		LogUtils.d("setPullLoadEnable");
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

	/**
	 * enable or disable pull down refresh feature.
	 * 
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) {
			mHeaderView.hide();
		} else {
			mHeaderView.show();
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

	/**
	 * 如果第二个可变参数不为空，则代表是自动刷新
	 * 
	 * @param delta
	 * @param during
	 */
	private void updateHeaderHeight(int currentY, int deltaY, int... during) {
		boolean isAutoRefresh = during != null && during.length > 0;
		if (isAutoRefresh) {
			mHeaderView.setState(XRefreshViewState.STATE_REFRESHING);
			startScroll(deltaY, during[0]);
		} else {
			if (mHolder.isOverHeader(deltaY)) {
				deltaY = -mHolder.mOffsetY;
			}
			moveView(deltaY);
			if (mEnablePullRefresh && !mPullRefreshing) {
				if (mHolder.mOffsetY > mHeaderViewHeight) {
					mHeaderView.setState(XRefreshViewState.STATE_READY);
				} else {
					mHeaderView.setState(XRefreshViewState.STATE_NORMAL);
				}
			}
		}
	}

	private void updateFooterHeight(int deltaY) {
		moveView(deltaY);
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

	/**
	 * 设置是否自动加载更多，默认是
	 * 
	 * @param autoLoadMore
	 *            true则自动刷新
	 */
	public void setAutoLoadMore(boolean autoLoadMore) {
		this.autoLoadMore = autoLoadMore;
	}

	public void startRefresh() {
		this.autoRefresh = true;
		mPullRefreshing = true;
		if (mRefreshViewListener != null) {
			mRefreshViewListener.onRefresh();
		}
		mContentView.scrollToTop();
		updateHeaderHeight(0, mHeaderViewHeight, 0);
	}

	/**
	 * reset header view's height.
	 */
	private void resetHeaderHeight() {
		float height = mHolder.mOffsetY;
		if (height == 0) // not visible.
			return;
		// refreshing and header isn't shown fully. do nothing.
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int offsetY = 0;
		if (mPullRefreshing) {
			offsetY = mHeaderViewHeight - mHolder.mOffsetY;
			startScroll(offsetY, SCROLL_DURATION);
		} else {
			offsetY = 0 - mHolder.mOffsetY;
			startScroll(offsetY, SCROLL_DURATION);
		}
		LogUtils.d("resetHeaderHeight offsetY=" + offsetY);
	}

	public void moveView(int deltaY) {
		mHolder.move(deltaY);
		mChild.offsetTopAndBottom(deltaY);
		mHeaderView.offsetTopAndBottom(deltaY);
		mFooterView.offsetTopAndBottom(deltaY);
		invalidate();
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			int lastScrollY = mHolder.mOffsetY;
			int currentY = mScroller.getCurrY();
			int offsetY = currentY - lastScrollY;
			lastScrollY = currentY;
			moveView(offsetY);

			LogUtils.d("currentY=" + currentY + ";mHolder.mOffsetY="
					+ mHolder.mOffsetY);
		} else {
			LogUtils.d("scroll end mOffsetY=" + mHolder.mOffsetY);
		}
	}

	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		LogUtils.d("stopRefresh mPullRefreshing=" + mPullRefreshing);
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();
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

	/**
	 * 设置并显示上次刷新的时间
	 */
	private void setRefreshTime() {
		if (lastRefreshTime <= 0) {
			return;
		}
		mHeaderView.setRefreshTime(lastRefreshTime);
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			startScroll(-mHolder.mOffsetY, 0);
		}
	}

	/**
	 * 
	 * @param offsetY
	 *            滑动偏移量，负数向上滑，正数反之
	 * @param duration
	 *            滑动持续时间
	 */
	public void startScroll(int offsetY, int duration) {
		mScroller.startScroll(0, mHolder.mOffsetY, 0, offsetY, duration);
		invalidate();
	}

	/**
	 * you can listener the child scroll state by invoking this method
	 * 
	 * @param listener
	 */
	public void setOnScrollListener(OnScrollListener listener) {
		mContentView.setOnScrollListener(listener);
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

		/**
		 * 用户手指释放的监听回调 direction >0: 下拉释放，<0:上拉释放
		 */
		public void onRelease(float direction);
	}

	public static class SimpleXRefreshListener implements XRefreshViewListener {

		@Override
		public void onRefresh() {

		}

		@Override
		public void onLoadMore() {

		}

		@Override
		public void onRelease(float direction) {
		}

	}
}