package com.andview.refreshview;

import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.andview.refreshview.XScrollView.OnScrollBottomListener;
import com.andview.refreshview.callback.IFooterCallBack;
import com.andview.refreshview.listener.OnBottomLoadMoreTime;
import com.andview.refreshview.listener.OnTopRefreshTime;
import com.andview.refreshview.recyclerview.BaseRecyclerAdapter;
import com.andview.refreshview.recyclerview.XSpanSizeLookup;
import com.andview.refreshview.utils.LogUtils;
import com.andview.refreshview.utils.Utils;

public class XRefreshContentView implements OnScrollListener, OnTopRefreshTime,
        OnBottomLoadMoreTime {
    private View child;
    private int mTotalItemCount;
    private OnTopRefreshTime mTopRefreshTime;
    private OnBottomLoadMoreTime mBottomLoadMoreTime;
    private XRefreshView mContainer;
    private OnScrollListener mAbsListViewScrollListener;
    private RecyclerView.OnScrollListener mRecyclerViewScrollListener;
    private XRefreshViewListener mRefreshViewListener;
    private RecyclerView.OnScrollListener mOnScrollListener;
    protected LAYOUT_MANAGER_TYPE layoutManagerType;

    private int mVisibleItemCount = 0;
    private int previousTotal = 0;
    private int mFirstVisibleItem;
    private int mLastVisibleItemPosition;
    private boolean mIsLoadingMore;
    private IFooterCallBack mFooterCallBack;
    private XRefreshViewState mState = XRefreshViewState.STATE_NORMAL;
    private Handler mHandler = new Handler();
    /**
     * 当已无更多数据时候，需把这个变量设为true
     */
    private boolean mHasLoadComplete = false;
    private int mPinnedTime;
    private XRefreshHolder mHolder;
    private XRefreshView mParent;

    public void setParent(XRefreshView parent) {
        mParent = parent;
    }

    public void setContentViewLayoutParams(boolean isHeightMatchParent,
                                           boolean isWidthMatchParent) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (isHeightMatchParent) {
            lp.height = LayoutParams.MATCH_PARENT;
        }
        if (isWidthMatchParent) {
            lp.height = LayoutParams.MATCH_PARENT;
        }
        // 默认设置宽高为match_parent
        child.setLayoutParams(lp);
    }

    public void setContentView(View child) {
        this.child = child;
        child.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
    }

    public View getContentView() {
        return child;
    }

    public void setHolder(XRefreshHolder holder) {
        mHolder = holder;
    }

    /**
     * 如果自动刷新，设置container, container!=null代表列表到达底部自动加载更多
     *
     * @param container
     */
    public void setContainer(XRefreshView container) {
        mContainer = container;
    }

    public void scrollToTop() {
        if (child instanceof AbsListView) {
            AbsListView absListView = (AbsListView) child;
            absListView.setSelection(0);
        } else if (child instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) child;
            RecyclerView.LayoutManager layoutManager = null;
            layoutManager = recyclerView.getLayoutManager();
            layoutManager.scrollToPosition(0);
        }
    }

    private boolean mSlienceLoadMore = false;

    public void setSlienceLoadMore(boolean slienceLoadMore) {
        mSlienceLoadMore = slienceLoadMore;
    }

    private boolean hasIntercepted = false;

    public void setScrollListener() {
        if (child instanceof AbsListView) {
            AbsListView absListView = (AbsListView) child;
            absListView.setOnScrollListener(this);
        } else if (child instanceof ScrollView) {
            setScrollViewScrollListener();

        } else if (child instanceof RecyclerView) {
            setRecyclerViewScrollListener();
        }
    }

    private void setScrollViewScrollListener() {
        if (child instanceof XScrollView) {
            XScrollView scrollView = (XScrollView) child;
            scrollView.registerOnBottomListener(new OnScrollBottomListener() {

                @Override
                public void srollToBottom() {
                    if (mSlienceLoadMore) {
                        if (mRefreshViewListener != null) {
                            mRefreshViewListener.onLoadMore(true);
                        }
                    } else if (mContainer != null && !hasLoadCompleted()) {
                        mContainer.invokeLoadMore();
                    }
                }
            });
        } else {
            throw new RuntimeException("please use XScrollView instead of ScrollView!");
        }
    }

    private void setRecyclerViewScrollListener() {
        layoutManagerType = null;
        final RecyclerView recyclerView = (RecyclerView) child;
        if (recyclerView.getAdapter() == null) {
            return;
        }
        if (!(recyclerView.getAdapter() instanceof BaseRecyclerAdapter)) {
            throw new RuntimeException("Recylerview的adapter请继承 BaseRecyclerAdapter");
        }
        final BaseRecyclerAdapter adapter = (BaseRecyclerAdapter) recyclerView.getAdapter();
        recyclerView.removeOnScrollListener(mOnScrollListener);
        mOnScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mRecyclerViewScrollListener != null) {
                    mRecyclerViewScrollListener.onScrollStateChanged(recyclerView, newState);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mRecyclerViewScrollListener != null) {
                    mRecyclerViewScrollListener.onScrolled(recyclerView, dx, dy);
                }
                if (mFooterCallBack == null && !mSlienceLoadMore) {
                    return;
                }
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                getRecyclerViewInfo(layoutManager);
                if (onRecyclerViewTop()) {
                    if (Utils.isRecyclerViewFullscreen(recyclerView)) {
//                        addFooterView(true);
                    } else {
                        mFooterCallBack.onStateReady();
                        mFooterCallBack.callWhenNotAutoLoadMore(mRefreshViewListener);
                    }
                    return;
                }
                LogUtils.d("test pre onScrolled mIsLoadingMore=" + mIsLoadingMore);
                if (mSlienceLoadMore) {
                    doSlienceLoadMore(adapter, layoutManager);
                } else {
                    if (!isOnRecyclerViewBottom()) {
                        mHideFooter = true;
                    }
                    ensureFooterShowWhenScrolling();
                    if (mParent != null && !mParent.getPullLoadEnable() && !hasIntercepted) {
                        addFooterView(false);
                        hasIntercepted = true;
                    }
                    if (hasIntercepted) {
                        return;
                    }
                    if (mContainer != null) {
                        doAutoLoadMore(adapter, layoutManager);
                    } else if (null == mContainer) {
                        doNormalLoadMore(adapter, layoutManager);
                    }
                }
            }
        };
        recyclerView.addOnScrollListener(mOnScrollListener);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager != null && layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new XSpanSizeLookup(adapter, gridLayoutManager.getSpanCount()));
        }
        initFooterCallBack(adapter);
    }

    private void initFooterCallBack(BaseRecyclerAdapter adapter) {
        if (!mSlienceLoadMore) {
            if (adapter != null) {
                View footerView = adapter.getCustomLoadMoreView();
                if (null == footerView) {
                    return;
                }
                mFooterCallBack = (IFooterCallBack) footerView;
                // 如果设置到达底部不自动加载更多，那么就点击footerview加载更多
                if (mFooterCallBack != null) {
                    mFooterCallBack.onStateReady();
                    mFooterCallBack.callWhenNotAutoLoadMore(mRefreshViewListener);
                }
            }
        }
    }

    private void doSlienceLoadMore(BaseRecyclerAdapter adapter, RecyclerView.LayoutManager layoutManager) {
        if (!mIsLoadingMore && isOnRecyclerViewBottom() && !hasLoadCompleted()) {
            if (mRefreshViewListener != null) {
                mIsLoadingMore = true;
                refreshAdapter(adapter, layoutManager);
                mRefreshViewListener.onLoadMore(true);
            }
        }
    }

    private void doAutoLoadMore(BaseRecyclerAdapter adapter, RecyclerView.LayoutManager layoutManager) {
        if (!mIsLoadingMore && isOnRecyclerViewBottom() && mHideFooter) {
            if (!hasLoadCompleted()) {
                if (mRefreshViewListener != null) {
                    refreshAdapter(adapter, layoutManager);
                    mRefreshViewListener.onLoadMore(false);
                }
                mIsLoadingMore = true;
                previousTotal = mTotalItemCount;
                mFooterCallBack.onStateRefreshing();
                setState(XRefreshViewState.STATE_LOADING);
            } else {
                loadCompleted();
            }
        } else {
            setState(XRefreshViewState.STATE_NORMAL);
        }
    }

    private boolean isFooterEnable() {
        if (mState != XRefreshViewState.STATE_COMPLETE && mParent != null && mParent.getPullLoadEnable()) {
            return true;
        }
        return false;
    }

    private void doNormalLoadMore(BaseRecyclerAdapter adapter, RecyclerView.LayoutManager layoutManager) {
        if (!mIsLoadingMore && isOnRecyclerViewBottom() && mHideFooter) {
            refreshAdapter(adapter, layoutManager);
            if (!hasLoadCompleted()) {
                if (mState != XRefreshViewState.STATE_READY && !addingFooter) {
                    mFooterCallBack.onStateReady();
                    setState(XRefreshViewState.STATE_READY);
                }
            } else {
                loadCompleted();
            }
        } else {
            setState(XRefreshViewState.STATE_NORMAL);
        }
    }

    public void notifyDatasetChanged() {
        final RecyclerView recyclerView = (RecyclerView) child;
        if (recyclerView.getAdapter() == null) {
            return;
        }
        if (!(recyclerView.getAdapter() instanceof BaseRecyclerAdapter)) {
            throw new RuntimeException("Recylerview的adapter请继承 BaseRecyclerAdapter");
        }
        final BaseRecyclerAdapter adapter = (BaseRecyclerAdapter) recyclerView.getAdapter();
        adapter.notifyDataSetChanged();
    }

    /**
     * 数据是否满一屏
     *
     * @return
     */
    private boolean onRecyclerViewTop() {
        if (isTop() && mFooterCallBack != null && mParent != null && mParent.getPullLoadEnable() && !hasLoadCompleted()) {
            return true;
        }
        return false;
    }

    private boolean mHideFooter = true;
    private boolean addingFooter = false;

    public void stopLoading(boolean hideFooter) {
        mIsLoadingMore = false;
        mTotalItemCount = 0;
        if (mFooterCallBack != null) {
            mFooterCallBack.onStateFinish(hideFooter);
            if (hideFooter) {
                if (child instanceof RecyclerView) {
                    final RecyclerView recyclerView = (RecyclerView) child;
                    final BaseRecyclerAdapter adapter = (BaseRecyclerAdapter) recyclerView.getAdapter();
                    if (adapter == null) return;
                    adapter.removeFooterView();
                    addFooterView(false);
                    addFooterView(true);
                }
            }
        }
        mHideFooter = hideFooter;
        mState = XRefreshViewState.STATE_FINISHED;
    }

    private boolean mRefreshAdapter = false;

    private boolean isOnRecyclerViewBottom() {
        if ((mTotalItemCount - 1 - mPreLoadCount) <= mLastVisibleItemPosition) {
            return true;
        }
        return false;
    }

    public void ensureFooterShowWhenScrolling() {
        if (isFooterEnable() && mFooterCallBack != null && !mFooterCallBack.isShowing()) {
            mFooterCallBack.show(true);
        }
    }

    private void refreshAdapter(BaseRecyclerAdapter adapter, RecyclerView.LayoutManager manager) {
        if (adapter != null && !mRefreshAdapter && !hasLoadCompleted()) {
            if (!(manager instanceof GridLayoutManager)) {
                View footerView = adapter.getCustomLoadMoreView();
                if (footerView != null) {
                    ViewGroup.LayoutParams layoutParams = footerView.getLayoutParams();
                    if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                        Utils.setFullSpan((StaggeredGridLayoutManager.LayoutParams) layoutParams);
                        mRefreshAdapter = true;
                    }
                }
            }
        }
    }

    public void getRecyclerViewInfo(RecyclerView.LayoutManager layoutManager) {
        int[] lastPositions = null;
        if (layoutManagerType == null) {
            if (layoutManager instanceof GridLayoutManager) {
                layoutManagerType = LAYOUT_MANAGER_TYPE.GRID;
            } else if (layoutManager instanceof LinearLayoutManager) {
                layoutManagerType = LAYOUT_MANAGER_TYPE.LINEAR;
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                layoutManagerType = LAYOUT_MANAGER_TYPE.STAGGERED_GRID;
            } else {
                throw new RuntimeException(
                        "Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
            }
        }
        mTotalItemCount = layoutManager.getItemCount();
        switch (layoutManagerType) {
            case LINEAR:
                mVisibleItemCount = layoutManager.getChildCount();
                mLastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            case GRID:
                mLastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                mFirstVisibleItem = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                break;
            case STAGGERED_GRID:
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                if (lastPositions == null)
                    lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];

                staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                mLastVisibleItemPosition = findMax(lastPositions);

                staggeredGridLayoutManager
                        .findFirstVisibleItemPositions(lastPositions);
                mFirstVisibleItem = findMin(lastPositions);
                break;
        }
    }


    /**
     * 静默加载时提前加载的item个数
     */
    private int mPreLoadCount;

    /**
     * 设置静默加载时提前加载的item个数
     *
     * @param count
     */
    public void setPreLoadCount(int count) {
        if (count < 0) {
            count = 0;
        }
        mPreLoadCount = count;
    }

    private boolean isHideFooterWhenComplete = true;

    protected void setHideFooterWhenComplete(boolean isHideFooterWhenComplete) {
        this.isHideFooterWhenComplete = isHideFooterWhenComplete;
    }

    public void loadCompleted() {
        if (mState != XRefreshViewState.STATE_COMPLETE) {
            mFooterCallBack.onStateComplete();
//            addFooterView(true);
            setState(XRefreshViewState.STATE_COMPLETE);
            mPinnedTime = mPinnedTime < 1000 ? 1000 : mPinnedTime;
            if (isHideFooterWhenComplete) {
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        addFooterView(false);
                    }
                }, mPinnedTime);
            }
        }
    }

    private void setState(XRefreshViewState state) {
        if (mState != XRefreshViewState.STATE_COMPLETE) {
            mState = state;
        }
    }

    public boolean hasLoadCompleted() {
        return mHasLoadComplete;
    }

    public void setLoadComplete(boolean hasComplete) {
        if (!hasComplete) {
            addFooterView(true);
        }
        mHasLoadComplete = hasComplete;
        setState(XRefreshViewState.STATE_NORMAL);
        if (!hasComplete) {
            mIsLoadingMore = false;
        }
    }

    private void addFooterView(boolean add) {
        if (!(child instanceof RecyclerView)) {
            if (mFooterCallBack != null) {
                mFooterCallBack.show(add);
            }
            return;
        }
        final RecyclerView recyclerView = (RecyclerView) child;
        if (recyclerView.getAdapter() != null && mFooterCallBack != null) {
            final BaseRecyclerAdapter adapter = (BaseRecyclerAdapter) recyclerView.getAdapter();
            if (add) {
                addingFooter = true;
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        //只有在footerview已经从Recyclerview中移除了以后才执行重新加入footerview的操作，不然Recyclerview的item布局会错乱
                        int index = recyclerView.indexOfChild(adapter.getCustomLoadMoreView());
                        if (index == -1) {
                            addingFooter = false;
                            if (isFooterEnable()) {
                                adapter.addFooterView();
                            }
                        } else {
                            recyclerView.post(this);
                        }
                    }
                });
            } else {
                adapter.removeFooterView();
            }
        }
    }

    /**
     * 设置显示和隐藏Recyclerview中的footerview
     *
     * @param enablePullLoad
     */
    public void setEnablePullLoad(boolean enablePullLoad) {
        addFooterView(enablePullLoad);
        hasIntercepted = false;
        mIsLoadingMore = false;
        mTotalItemCount = 0;
        if (enablePullLoad) {
            if (onRecyclerViewTop()) {
                if (child instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) child;
                    if (!Utils.isRecyclerViewFullscreen(recyclerView)) {
                        mFooterCallBack.onStateReady();
                        mFooterCallBack.callWhenNotAutoLoadMore(mRefreshViewListener);
                    }
                }
            }
        }
    }

    public void setPinnedTime(int pinnedTime) {
        mPinnedTime = pinnedTime;
    }

    public void setOnAbsListViewScrollListener(OnScrollListener listener) {
        mAbsListViewScrollListener = listener;
    }

    public void setOnRecyclerViewScrollListener(RecyclerView.OnScrollListener listener) {
        mRecyclerViewScrollListener = listener;
    }

    public void setXRefreshViewListener(XRefreshViewListener refreshViewListener) {
        mRefreshViewListener = refreshViewListener;
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
     * @param topRefreshTime
     */
    public void setOnTopRefreshTime(OnTopRefreshTime topRefreshTime) {
        this.mTopRefreshTime = topRefreshTime;
    }

    /**
     * 设置底部监听
     *
     * @param bottomLoadMoreTime
     */
    public void setOnBottomLoadMoreTime(OnBottomLoadMoreTime bottomLoadMoreTime) {
        this.mBottomLoadMoreTime = bottomLoadMoreTime;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mSlienceLoadMore) {
            if (mRefreshViewListener != null && !hasLoadCompleted() && !mIsLoadingMore && mTotalItemCount - 1 <= view.getLastVisiblePosition() + mPreLoadCount) {
                mRefreshViewListener.onLoadMore(true);
                mIsLoadingMore = true;
            }
        } else if (mContainer != null && !hasLoadCompleted()
                && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (mPreLoadCount == 0) {
                if (isBottom()) {
                    if (!mIsLoadingMore) {
                        mIsLoadingMore = mContainer.invokeLoadMore();
                    }
                }
            } else {
                if (mTotalItemCount - 1 <= view.getLastVisiblePosition() + mPreLoadCount) {
                    if (!mIsLoadingMore) {
                        mIsLoadingMore = mContainer.invokeLoadMore();
                    }
                }
            }
        }
        if (mAbsListViewScrollListener != null) {
            mAbsListViewScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        mTotalItemCount = totalItemCount;
        if (mAbsListViewScrollListener != null) {
            mAbsListViewScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
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

    public boolean isLoading() {
        if (mSlienceLoadMore) {
            return false;
        }
        return mIsLoadingMore;
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
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
                    || webview.getContentHeight() * webview.getScale() != webview.getHeight() + webview.getScrollY();
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
     * @param view      v
     * @param direction 方向 负数代表向上滑动 ，正数则反之
     * @return
     */
    public boolean canScrollVertically(View view, int direction) {
        return ViewCompat.canScrollVertically(view, direction);
    }

    public void offsetTopAndBottom(int offset) {
        child.offsetTopAndBottom(offset);
    }

    public boolean isRecyclerView() {
        if (mSlienceLoadMore) {
            return false;
        } else if (null != child && child instanceof RecyclerView) {
            return true;
        }
        return false;
    }

    private int findMax(int[] lastPositions) {
        int max = Integer.MIN_VALUE;
        for (int value : lastPositions) {
            if (value > max)
                max = value;
        }
        return max;
    }

    private int findMin(int[] lastPositions) {
        int min = Integer.MAX_VALUE;
        for (int value : lastPositions) {
            if (value != RecyclerView.NO_POSITION && value < min)
                min = value;
        }
        return min;
    }

    public enum LAYOUT_MANAGER_TYPE {
        LINEAR, GRID, STAGGERED_GRID
    }

}