package com.andview.refreshview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
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

import com.andview.refreshview.callback.IFooterCallBack;
import com.andview.refreshview.callback.IHeaderCallBack;
import com.andview.refreshview.listener.OnBottomLoadMoreTime;
import com.andview.refreshview.listener.OnTopRefreshTime;
import com.andview.refreshview.utils.LogUtils;
import com.andview.refreshview.utils.Utils;

import java.util.Calendar;

public class XRefreshView extends LinearLayout {
    // -- header view
    private View mHeaderView;

    private int mHeaderViewHeight; // header view's height
    /**
     * 最初的滚动位置.第一次布局时滚动header的高度的距离
     */
    protected int mInitScrollY = 0;
    private int mLastY = -1; // save event y
    private int mLastX = -1; // save event x
    private boolean mEnablePullRefresh = true;
    public boolean mPullRefreshing = false; // is refreashing.
    private float OFFSET_RADIO = 1.8f; // support iOS like pull

    private int SCROLL_DURATION = 400; // scroll back duration
    private XRefreshViewListener mRefreshViewListener;
    // -- footer view
    private View mFooterView;
    private boolean mEnablePullLoad;
    public boolean mPullLoading;
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
    private IHeaderCallBack mHeaderCallBack;
    private IFooterCallBack mFooterCallBack;
    /**
     * 当刷新完成以后，headerview和footerview被固定的时间，在这个时间以后headerview才会回弹
     */
    private int mPinnedTime;
    /**
     * 有没有滚回初始位置
     */
    private boolean mHasScrollBack;
    private Handler mHandler = new Handler();
    private XRefreshViewState mState = null;
    /**
     * 当已无更多数据时候，需把这个变量设为true
     */
    private boolean mHasLoadComplete = false;
    /**
     * 在刷新的时候是否可以移动contentView
     */
    private boolean mIsPinnedContentWhenRefreshing = true;

    private int MOVE_MODE;
    private static final int MOVE_HEAD=0;
    private static final int MOVE_FOOT=1;

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

    /**
     * 如果被刷新的view是RecyclerView，那么footerView将被添加到adapter中，不会添加到这里
     *
     * @return
     */
    public boolean needAddFooterView() {
        return !mContentView.isRecyclerView();
    }

    /**
     * pass true if need use for Horizontal move, or false
     *
     * @param isForHorizontalMove default false
     */
    public void setMoveForHorizontal(boolean isForHorizontalMove) {
        this.isForHorizontalMove = isForHorizontalMove;
    }

    private void initWithContext(Context context, AttributeSet attrs) {

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
        mHeaderView = new XRefreshViewHeader(getContext());
        mFooterView = new XRefreshViewFooter(context);
        this.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        addHeaderView();
                        addFooterView();
                        // 移除视图树监听器
                        removeViewTreeObserver(this);
                    }
                });
    }

    private void addHeaderView() {
        addView(mHeaderView, 0);
        mHeaderView.measure(0, 0);
        mHeaderViewHeight = ((IHeaderCallBack) mHeaderView).getHeaderHeight();
        Utils.setTopMargin(mHeaderView, -mHeaderViewHeight);
        mContentView.setContentView(XRefreshView.this.getChildAt(1));
        if (autoLoadMore) {
            mContentView.setContainer(this);
        } else {
            mContentView.setContainer(null);
        }
        mContentView.setContentViewLayoutParams(isHeightMatchParent,
                isWidthMatchParent);
        mHeaderCallBack = (IHeaderCallBack) mHeaderView;
        mFooterCallBack = (IFooterCallBack) mFooterView;
        checkPullRefreshEnable();
        checkPullLoadEnable();
    }

    private void addFooterView() {
        LogUtils.d("onGlobalLayout mHeaderViewHeight=" + mHeaderViewHeight);
        mContentView.setHolder(mHolder);
        mContentView.setScrollListener();
        if (mEnablePullLoad && needAddFooterView()) {
            addView(mFooterView);
            mFooterView.measure(0, 0);
            mFootHeight = ((IFooterCallBack) mFooterView).getFooterHeight();
            Log.i("CustomView", "add footView" + ";mFootHeight=" + mFootHeight);
            Utils.setBottomMargin(mFooterView, -mFootHeight);
        }
        if (autoRefresh) {
            startRefresh();
        }
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

    private boolean isIntercepted;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        int deltaY = 0;
        int deltaX = 0;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mHasSendCancelEvent = false;
                mHasSendDownEvent = false;
                mLastY = (int) ev.getRawY();
                mLastX = (int) ev.getRawX();
                mInitialMotionY = mLastY;

                // if (!mScroller.isFinished() && !mPullRefreshing && !mPullLoading)
                // {
                // mScroller.forceFinished(true);
                // }
                break;
            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent = ev;
                if (mPullLoading || mPullRefreshing || !isEnabled() || mIsIntercept
                        || mHasScrollBack || mContentView.isLoading()) {
                    if (mIsPinnedContentWhenRefreshing) {
                        return super.dispatchTouchEvent(ev);
                    } else {
                        sendCancelEvent();
                        return true;
                    }
                }
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
                } else if (needAddFooterView() && mContentView.isBottom()
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
                if (mHolder.hasHeaderPullDown() && !mHasScrollBack) {
                    // invoke refresh
                    if (mEnablePullRefresh && mHolder.mOffsetY > mHeaderViewHeight) {
                        mPullRefreshing = true;
                        mHeaderCallBack.onStateRefreshing();
                        mState = XRefreshViewState.STATE_REFRESHING;
                        if (mRefreshViewListener != null) {
                            mRefreshViewListener.onRefresh();
                        }
                    }
                    resetHeaderHeight();
                } else if (mHolder.hasFooterPullUp()) {
                    if (mEnablePullLoad && needAddFooterView() && !mHasLoadComplete) {
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

    // @Override
    // public boolean onInterceptTouchEvent(MotionEvent ev) {
    // if(mPullRefreshing||mPullLoading){
    // return !mIsPinnedContentWhenRefreshing;
    // }
    // return super.onInterceptTouchEvent(ev);
    // }

    public void invoketLoadMore() {
        if (mEnablePullLoad && !mPullLoading && !mPullRefreshing
                && !mHasScrollBack && !mHasLoadComplete) {
            mFooterCallBack.onStateRefreshing();
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
     * enable or disable pull up load more feature.
     *
     * @param enable
     */
    public void setPullLoadEnable(boolean enable) {
        LogUtils.d("setPullLoadEnable");
        mEnablePullLoad = enable;
    }

    /**
     * enable or disable pull down refresh feature.
     *
     * @param enable
     */
    public void setPullRefreshEnable(boolean enable) {
        mEnablePullRefresh = enable;
    }

    private void checkPullRefreshEnable() {
        if (!mEnablePullRefresh) {
            mHeaderCallBack.hide();
        } else {
            mHeaderCallBack.show();
        }
    }

    private void checkPullLoadEnable() {
        if (!mEnablePullLoad) {
            mFooterCallBack.hide();
        } else {
            mPullLoading = false;
            mFooterCallBack.show();
            mFooterCallBack.onStateRefreshing();
        }
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
     * @param currentY
     * @param deltaY
     * @param during
     */
    private void updateHeaderHeight(int currentY, int deltaY, int... during) {
        MOVE_MODE = MOVE_HEAD;
        boolean isAutoRefresh = during != null && during.length > 0;
        if (isAutoRefresh) {
            mHeaderCallBack.onStateRefreshing();
            startScroll(deltaY, during[0]);
        } else {
            if (mHolder.isOverHeader(deltaY)) {
                deltaY = -mHolder.mOffsetY;
            }
            moveView(deltaY);
            if (mEnablePullRefresh && !mPullRefreshing) {
                if (mHolder.mOffsetY > mHeaderViewHeight) {
                    if (mState != XRefreshViewState.STATE_READY) {
                        mHeaderCallBack.onStateReady();
                        mState = XRefreshViewState.STATE_READY;
                    }
                } else {
                    if (mState != XRefreshViewState.STATE_NORMAL) {
                        mHeaderCallBack.onStateNormal();
                        mState = XRefreshViewState.STATE_NORMAL;
                    }
                }
            }
        }
    }

    private void updateFooterHeight(int deltaY) {
        MOVE_MODE = MOVE_FOOT;
        if (mState != XRefreshViewState.STATE_READY && !autoLoadMore) {
            mFooterCallBack.onStateReady();
            mState = XRefreshViewState.STATE_READY;
        }
        moveView(deltaY);
    }

    /**
     * 设置是否自动刷新，默认不自动刷新
     *
     * @param autoRefresh true则自动刷新
     */
    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
        setRefreshTime();
    }

    /**
     * 设置是否自动加载更多，默认是
     *
     * @param autoLoadMore true则自动刷新
     */
    public void setAutoLoadMore(boolean autoLoadMore) {
        this.autoLoadMore = autoLoadMore;
    }

    public void startRefresh() {
        if (mHolder.mOffsetY != 0 || mContentView.isLoading() || !isEnabled()) {
            return;
        }
        // 如果条件成立，代表布局还没有初始化完成，改变标记，等待该方法再次调用，完成开始刷新
        if (mHeaderCallBack == null) {
            this.autoRefresh = true;
        } else {
            mPullRefreshing = true;
            if (mRefreshViewListener != null) {
                mRefreshViewListener.onRefresh();
            }
            mContentView.scrollToTop();
            updateHeaderHeight(0, mHeaderViewHeight, 0);
        }
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

    public void moveHeader(int deltaY) {
        LogUtils.i("moveHeader");
        Utils.setTopMargin(mHeaderView, deltaY);
    }

    public void moveFooter(int deltaY) {
        LogUtils.i("moveFooter");
        if(needAddFooterView()){
            Utils.setBottomMargin(mFooterView, deltaY);
        }
    }

    public void moveView(int deltaY) {
        mHolder.move(deltaY);
        if(MOVE_MODE==MOVE_HEAD){
            moveHeader(deltaY);
        }else if(MOVE_MODE==MOVE_FOOT){
            moveFooter(deltaY);
        }
//        mHeaderView.offsetTopAndBottom(deltaY);
//        mContentView.offsetTopAndBottom(deltaY);
//        if (needAddFooterView()) {
//            mFooterView.offsetTopAndBottom(deltaY);
//        }
//        invalidate();

        if (mRefreshViewListener != null
                && (mContentView.isTop() || mPullRefreshing)) {
            double offset = 1.0 * mHolder.mOffsetY / mHeaderViewHeight;
            offset = offset > 1 ? 1 : offset;
            mRefreshViewListener.onHeaderMove(offset, mHolder.mOffsetY);
            mHeaderCallBack.onHeaderMove(offset, mHolder.mOffsetY);
        }
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
            if (mHolder.mOffsetY == 0)
                mHasScrollBack = false;
        }
    }

    /**
     * stop refresh, reset header view.
     */
    public void stopRefresh() {
        LogUtils.i("stopRefresh mPullRefreshing=" + mPullRefreshing);
        if (mPullRefreshing == true) {
            mPullRefreshing = false;
            mHeaderCallBack.onStateFinish();
            mState = XRefreshViewState.STATE_COMPLETE;
            mHasScrollBack = true;
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    resetHeaderHeight();
                    lastRefreshTime = Calendar.getInstance().getTimeInMillis();
                }
            }, mPinnedTime);
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
        mHeaderCallBack.setRefreshTime(lastRefreshTime);
    }

    /**
     * stop load more, reset footer view.
     */
    public void stopLoadMore() {
        if (needAddFooterView()) {
            if (mPullLoading == true) {
                mPullLoading = false;
                mFooterCallBack.onStateFinish();
                mState = XRefreshViewState.STATE_COMPLETE;
                if (mPinnedTime >= 1000) {// 在加载更多完成以后，只有mPinnedTime大于1s才生效，不然效果不好
                    mHasScrollBack = true;
                    mHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            endLoadMore();
                        }
                    }, mPinnedTime);
                } else {
                    endLoadMore();
                }
            }
        } else {
            mContentView.stopLoading();
        }
    }

    public void setLoadComplete(boolean hasComplete) {
        mHasLoadComplete = hasComplete;
        if (needAddFooterView()) {
            stopLoadMore();
            if (hasComplete) {
                mFooterCallBack.hide();
                mFooterCallBack.onStateComplete();
            } else {
                mFooterCallBack.onStateRefreshing();
                mFooterCallBack.show();
            }
        } else {
            mContentView.setLoadComplete(hasComplete);
        }
    }

    public boolean hasLoadCompleted() {
        return mHasLoadComplete;
    }

    public void endLoadMore() {
        startScroll(-mHolder.mOffsetY, 0);
        mFooterCallBack.onStateRefreshing();
        if (mHasLoadComplete) {
            mFooterCallBack.hide();
        }
    }

    /**
     * @param offsetY  滑动偏移量，负数向上滑，正数反之
     * @param duration 滑动持续时间
     */
    public void startScroll(int offsetY, int duration) {
        mHasScrollBack = true;
        if (offsetY != 0) {
            mScroller.startScroll(0, mHolder.mOffsetY, 0, offsetY, duration);
            invalidate();
        }
    }

    /**
     * 设置Abslistview的滚动监听事件
     *
     * @param scrollListener
     */
    public void setOnAbsListViewScrollListener(OnScrollListener scrollListener) {
        mContentView.setOnAbsListViewScrollListener(scrollListener);
    }

    /**
     * 设置Recylerview的滚动监听事件
     */
    public void setOnRecyclerViewScrollListener(
            RecyclerView.OnScrollListener scrollListener) {
        mContentView.setOnRecyclerViewScrollListener(scrollListener);
    }

    public void setXRefreshViewListener(XRefreshViewListener l) {
        mRefreshViewListener = l;
        mContentView.setXRefreshViewListener(l);
    }

    public void setFooterCallBack(IFooterCallBack footerCallBack) {
        mFooterCallBack = footerCallBack;
    }

    /**
     * 设置headerview回滚的时间，默认400毫秒
     *
     * @param during
     */
    public void setScrollDuring(int during) {
        SCROLL_DURATION = during;
    }

    /**
     * 设置阻尼系数，建议使用默认的
     *
     * @param ratio 默认 1.8
     */
    public void setDampingRatio(float ratio) {
        OFFSET_RADIO = ratio;
    }

    /**
     * 设置当下拉刷新完成以后，headerview和footerview被固定的时间
     * 注:考虑到ui效果，只有时间大于1s的时候，footerview被固定的效果才会生效
     *
     * @param pinnedTime
     */
    public void setPinnedTime(int pinnedTime) {
        mPinnedTime = pinnedTime;
        mContentView.setPinnedTime(pinnedTime);
    }

    /**
     * 设置在刷新的时候是否可以移动contentView
     *
     * @param isPinned true 固定不移动 反之，可以移动
     */
    public void setPinnedContent(boolean isPinned) {
        mIsPinnedContentWhenRefreshing = !isPinned;
    }

    /**
     * 设置自定义headerView
     *
     * @param headerView headerView必须要实现 IHeaderCallBack接口
     */
    public void setCustomHeaderView(View headerView) {
        LogUtils.i("setCustomHeaderView");
        if (headerView instanceof IHeaderCallBack) {
            mHeaderView = headerView;
        } else {
            throw new RuntimeException(
                    "headerView must be implementes IHeaderCallBack!");
        }
    }

    /**
     * 设置自定义footerView
     *
     * @param footerView footerView必须要实现 IFooterCallBack接口
     */
    public void setCustomFooterView(View footerView) {
        if (footerView instanceof IFooterCallBack) {
            mFooterView = footerView;
        } else {
            throw new RuntimeException(
                    "footerView must be implementes IFooterCallBack!");
        }
    }

    /**
     * implements this interface to get refresh/load more event.
     */
    public interface XRefreshViewListener {
        public void onRefresh();

        public void onLoadMore();

        /**
         * 用户手指释放的监听回调
         *
         * @param direction >0: 下拉释放，<0:上拉释放 注：暂时没有使用这个方法
         */
        public void onRelease(float direction);

        /**
         * 获取headerview显示的高度与headerview高度的比例
         *
         * @param offset  移动距离和headerview高度的比例，范围是0~1，0：headerview完全没显示
         *                1：headerview完全显示
         * @param offsetY headerview移动的距离
         */
        public void onHeaderMove(double offset, int offsetY);
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

        @Override
        public void onHeaderMove(double offset, int offsetY) {

        }

    }
}