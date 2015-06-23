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
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;

import com.andview.refreshview.base.XRefreshFooterViewBase;
import com.andview.refreshview.base.XRefreshHeaderViewBase;
import com.andview.refreshview.listener.OnBottomLoadMoreTime;
import com.andview.refreshview.listener.OnTopRefreshTime;
import com.andview.refreshview.utils.LogUtils;
import com.andview.refreshview.utils.Utils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;

public class XRefreshView extends LinearLayout {
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

	private final static int SCROLL_DURATION = 400; // scroll back duration
	// the interface to trigger refresh and load more.
	private XRefreshViewListener mRefreshViewListener;
	// -- footer view
	private XRefreshViewFooter mFooterView;
	private boolean mEnablePullLoad;
	public boolean mPullLoading;
	/**
	 * 在开始上拉加载更多的时候，记录下childView一开始的Y轴坐标
	 */
	private float mOriginChildY = -1;
	/**
	 * 在开始上拉加载更多的时候，记录下FootView一开始的Y轴坐标
	 */
	private float mOriginFootY = -1;
	/**
	 * 在开始上拉加载更多的时候，记录下HeadView一开始的Y轴坐标
	 */
	private float mOriginHeadY = -1;

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
	private int mFootHeight;
	/**
	 * 被刷新的view
	 */
	private XRefreshContentView mContentView;
	private boolean isHeightMatchParent = true;
	private boolean isWidthMatchParent = true;
	private float mInitialMotionY;
	private int mTouchSlop;
	private float lastChidY;
	private float lastFootY;
	private float lastHeaderY;

	public XRefreshView(Context context) {
		this(context, null);
	}

	public XRefreshView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClickable(true);
		setLongClickable(true);
		animaListener = new AnimaListener();
		mContentView = new XRefreshContentView();
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

	/**
	 * 设置底部加载更多时机
	 * 
	 * 现阶段XRefreshView对于上拉加载时机的判断仅支持api14也就是安卓4.0 以上的版本，
	 * 如果想要兼容4.0以下，得调用此方法自己设置上拉加载的时机
	 * 
	 * @param bottomListener
	 */
	public void setOnBottomLoadMoreTime(OnBottomLoadMoreTime bottomListener) {
		mContentView.setOnBottomLoadMoreTime(bottomListener);
	}

	@Override
	protected void onFinishInflate() {
		child = mContentView.setContentView(XRefreshView.this.getChildAt(1));
		mContentView.setContentViewLayoutParams(isHeightMatchParent,
				isWidthMatchParent);
		super.onFinishInflate();
	}

	private void initWithContext(Context context, AttributeSet attrs) {
		LogUtils.i("initWithContext");
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

						mOriginHeadY = getTop() - mHeaderViewHeight;
						mOriginChildY = getTop();
						lastChidY = mOriginChildY;
						lastHeaderY = mOriginHeadY;

						LogUtils.i("onGlobalLayout mHeaderViewHeight="
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
	 * 丈量视图的宽、高。宽度为用户设置的宽度，高度则为header, content view, footer这三个子控件的高度只和。
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mOriginHeadY > 0) {
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
		if (mOriginHeadY > 0) {
			return;
		}
		mFootHeight = mFooterView.getMeasuredHeight();
		int childCount = getChildCount();
		int top = getPaddingTop();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child == mHeaderView) {
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

	/*
	 * 在适当的时候拦截触摸事件，这里指的适当的时候是当mContentView滑动到顶部，并且是下拉时拦截触摸事件，否则不拦截，交给其child
	 * view 来处理。
	 * 
	 * @see
	 * android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = MotionEventCompat.getActionMasked(ev);
		switch (action) {

		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			if (mPullLoading || mPullRefreshing || animaDoing) {
				return super.onInterceptTouchEvent(ev);
			}
			final float deltaY = ev.getRawY() - mLastY;

			// intercept the MotionEvent only when user is not scrolling
			if (Math.abs(deltaY) < mTouchSlop) {
				return super.onInterceptTouchEvent(ev);
			}
			LogUtils.d("isTop=" + mContentView.isTop() + ";isBottom="
					+ mContentView.isBottom());
			// 如果拉到了顶部, 并且是下拉,则拦截触摸事件,从而转到onTouchEvent来处理下拉刷新事件
			if (mContentView.isTop() && deltaY > 0) {
				mInitialMotionY = mLastY;
				if (mInitialMotionY <= 0) {
					mInitialMotionY = ev.getRawY();
				}
				LogUtils.d("mInitialMotionY=" + mInitialMotionY + ";getrawY="
						+ ev.getRawY());
				setRefreshTime();
				return true;
			} else if (mContentView.isBottom() && deltaY < 0) {
				mInitialMotionY = mLastY;
				LogUtils.d("mInitialMotionY=" + mInitialMotionY + ";getrawY="
						+ ev.getRawY());
				return true;
			}
			break;
		}
		return super.onInterceptTouchEvent(ev);
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

	float mOffsetY;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			float currentY = ev.getRawY();
			float deltaY = currentY - mLastY;
			mLastY = currentY;
			mOffsetY = (currentY + deltaY - mInitialMotionY) / OFFSET_RADIO;
			if (mContentView.isTop() && (deltaY > 0 || mOffsetY > 0)) {
				updateHeaderHeight(currentY, mOffsetY);
			} else if (mContentView.isBottom() && deltaY < 0 && mEnablePullLoad) {
				updateFooterHeight(currentY, mOffsetY);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (mContentView.isTop() && mOffsetY > 0) {
				// invoke refresh
				if (!mPullRefreshing && mEnablePullRefresh
						&& mOffsetY > mHeaderViewHeight) {
					mPullRefreshing = true;
					mHeaderView.setState(XRefreshViewState.STATE_REFRESHING);
					if (mRefreshViewListener != null) {
						mRefreshViewListener.onRefresh();
					}
				}
				resetHeaderHeight();
			} else if (mContentView.isBottom()) {
				if (!mPullLoading && mEnablePullLoad) {
					Utils.moveChildAndAddedView(child, mFooterView, lastChidY,
							mOriginChildY - mFootHeight, lastFootY,
							mOriginFootY - mFootHeight, SCROLL_DURATION);
					lastChidY = mOriginChildY - mFootHeight;
					lastFootY = mOriginFootY - mFootHeight;
					startLoadMore();
				}
			}
			if (mRefreshViewListener != null) {
				mRefreshViewListener.onRelease(mOffsetY);
			}
			mLastY = -1; // reset
			mInitialMotionY = 0;
			// mChildY = -1;
			// mFootY = -1;
			break;
		}
		return super.onTouchEvent(ev);
	}

	float mCurrentChildY;
	public float mCurrentHeadY;

	/**
	 * 如果第二个可变参数不为空，则代表是自动刷新
	 * 
	 * @param delta
	 * @param during
	 */
	private void updateHeaderHeight(float currentY, float offsetY,
			int... during) {
		mCurrentChildY = mOriginChildY + offsetY;
		mCurrentHeadY = mOriginHeadY + offsetY;
		if (mCurrentHeadY <= mOriginHeadY) {
			mCurrentHeadY = mOriginHeadY;
			return;
		}
		LogUtils.i("offsetY=" + offsetY + ";lastHeaderY=" + lastHeaderY
				+ "mOriginHeadY=" + mOriginHeadY);
		if (during != null && during.length > 0) {
			mHeaderView.setState(XRefreshViewState.STATE_REFRESHING);
			Utils.moveChildAndAddedView(child, mHeaderView, lastChidY,
					mCurrentChildY, lastHeaderY, mCurrentHeadY, during[0]);
		} else {
			Utils.moveChildAndAddedView(child, mHeaderView, lastChidY,
					mCurrentChildY, lastHeaderY, mCurrentHeadY, 0);
			if (mEnablePullRefresh && !mPullRefreshing) {
				if (offsetY > mHeaderViewHeight) {
					mHeaderView.setState(XRefreshViewState.STATE_READY);
				} else {
					mHeaderView.setState(XRefreshViewState.STATE_NORMAL);
				}
			}
		}
		lastChidY = mCurrentChildY;
		lastHeaderY = mCurrentHeadY;
	}

	private void updateFooterHeight(float currentY, float offsetY) {
		if (mOriginChildY == -1 || mOriginFootY == -1) {
			mOriginFootY = mFooterView.getTop();
			lastFootY = mOriginFootY;
		}
		float childY = mOriginChildY + offsetY;
		float footY = mOriginFootY + offsetY;
		LogUtils.i("mOriginFootY=" + mOriginFootY + ";footY=" + footY);
		Utils.moveChildAndAddedView(child, mFooterView, lastChidY, childY,
				lastFootY, footY, 0);
		lastChidY = childY;
		lastFootY = footY;
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
		mOffsetY = mHeaderViewHeight;
		updateHeaderHeight(0, mOffsetY, SCROLL_DURATION);
	}

	/**
	 * reset header view's height.
	 */
	private void resetHeaderHeight() {
		float height = mOffsetY;
		if (height == 0) // not visible.
			return;
		// refreshing and header isn't shown fully. do nothing.
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int headHeight = mHeaderViewHeight;
		if (mPullRefreshing) {
			Utils.moveChildAndAddedView(child, mHeaderView, lastChidY,
					mOriginChildY + headHeight, lastHeaderY, mOriginHeadY
							+ headHeight, SCROLL_DURATION);
			lastChidY = mOriginChildY + headHeight;
			lastHeaderY = mOriginHeadY + headHeight;
		} else {
			Utils.moveChildAndAddedView(child, mHeaderView, lastChidY,
					mOriginChildY, lastHeaderY, mOriginHeadY, SCROLL_DURATION,
					animaListener);
			lastChidY = mOriginChildY;
			lastHeaderY = mOriginHeadY;
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
			mCurrentHeadY = mOriginHeadY;
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
		mHeaderView.setRefreshTime(lastRefreshTime);
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			Utils.moveChildAndAddedView(child, mFooterView, lastChidY,
					mOriginChildY, lastFootY, mOriginFootY, 0, animaListener);
			lastChidY = mOriginChildY;
			lastFootY = mOriginFootY;
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