package com.andview.refreshview;

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
import com.andview.refreshview.callback.IFooterCallBack;
import com.andview.refreshview.listener.OnBottomLoadMoreTime;
import com.andview.refreshview.listener.OnTopRefreshTime;
import com.andview.refreshview.recyclerview.BaseRecyclerAdapter;
import com.andview.refreshview.recyclerview.XSpanSizeLookup;
import com.andview.refreshview.utils.LogUtils;
import com.andview.refreshview.utils.Utils;
import com.andview.refreshview.view.XWebView;

public class XRefreshContentView implements OnScrollListener, OnTopRefreshTime, OnBottomLoadMoreTime {
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
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            layoutManager.scrollToPosition(0);
        }
    }

    private boolean mSilenceLoadMore = false;

    public void setSilenceLoadMore(boolean silenceLoadMore) {
        mSilenceLoadMore = silenceLoadMore;
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
            scrollView.setOnScrollListener(mParent, new XScrollView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(ScrollView view, int scrollState, boolean arriveBottom) {
                    if (scrollState == SCROLL_STATE_IDLE && arriveBottom) {
                        if (mSilenceLoadMore) {
                            if (mRefreshViewListener != null) {
                                mRefreshViewListener.onLoadMore(true);
                            }
                        } else if (mContainer != null && !hasLoadCompleted()) {
                            mContainer.invokeLoadMore();
                        }
                    }
                }

                @Override
                public void onScroll(int l, int t, int oldl, int oldt) {

                }
            });
        } else {
            throw new RuntimeException("please use XScrollView instead of ScrollView!");
        }
    }

    public void onRecyclerViewScrolled(RecyclerView recyclerView, BaseRecyclerAdapter adapter, int dx, int dy, boolean force) {
        if (mRecyclerViewScrollListener != null) {
            mRecyclerViewScrollListener.onScrolled(recyclerView, dx, dy);
        }
        if (mFooterCallBack == null && !mSilenceLoadMore || adapter == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        getRecyclerViewInfo(layoutManager);
        refreshAdapter(adapter, layoutManager);
        LogUtils.d("test pre onScrolled mIsLoadingMore=" + mIsLoadingMore);
        if (onRecyclerViewTop()) {
            if (Utils.isRecyclerViewFullscreen(recyclerView)) {
//                        addFooterView(true);
            } else {
                if (mHideFooter) {
                    mFooterCallBack.onStateReady();
                    mFooterCallBack.callWhenNotAutoLoadMore(mParent);
                }
            }
            return;
        }
        if (dy == 0 && !force) {
            return;
        }
        if (mSilenceLoadMore) {
            doSilenceLoadMore(adapter, layoutManager);
        } else {
            if (!isOnRecyclerViewBottom()) {
                mHideFooter = true;
            }
            if (mParent != null && !mParent.getPullLoadEnable() && !hasIntercepted) {
                addFooterView(false);
                hasIntercepted = true;
            }
            if (hasIntercepted) {
                return;
            }
            ensureFooterShowWhenScrolling();
            if (mContainer != null) {
                doAutoLoadMore(adapter, layoutManager);
            } else if (null == mContainer) {
                doNormalLoadMore(adapter, layoutManager);
            }
        }
    }

    private static final String RECYCLERVIEW_ADAPTER_WARIN = "Recylerview的adapter请继承 BaseRecyclerAdapter,否则不能使用封装的Recyclerview的相关特性";

    private BaseRecyclerAdapter getRecyclerApdater(RecyclerView recyclerView) {
        BaseRecyclerAdapter adapter = null;
        if (recyclerView.getAdapter() instanceof BaseRecyclerAdapter) {
            adapter = (BaseRecyclerAdapter) recyclerView.getAdapter();
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager != null && layoutManager instanceof GridLayoutManager) {
                GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                gridLayoutManager.setSpanSizeLookup(new XSpanSizeLookup(adapter, gridLayoutManager.getSpanCount()));
            }
            adapter.insideEnableFooter(mParent.getPullLoadEnable());
            initFooterCallBack(adapter, mParent);
        }
        return adapter;
    }

    private BaseRecyclerAdapter mRecyclerApdater;

    private void setRecyclerViewScrollListener() {
        layoutManagerType = null;
        final RecyclerView recyclerView = (RecyclerView) child;
        if (recyclerView.getAdapter() != null) {
            if (recyclerView.getAdapter() instanceof BaseRecyclerAdapter) {
                mRecyclerApdater = getRecyclerApdater(recyclerView);
            } else {
                LogUtils.w(RECYCLERVIEW_ADAPTER_WARIN);
            }
        }
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
                if (mRecyclerApdater == null && recyclerView.getAdapter() != null && recyclerView.getAdapter() instanceof BaseRecyclerAdapter) {
                    mRecyclerApdater = getRecyclerApdater(recyclerView);
                }
                onRecyclerViewScrolled(recyclerView, mRecyclerApdater, dx, dy, false);
            }
        };
        recyclerView.addOnScrollListener(mOnScrollListener);
    }

    public void initFooterCallBack(BaseRecyclerAdapter adapter, XRefreshView parent) {
        if (!mSilenceLoadMore) {
            if (adapter != null) {
                View footerView = adapter.getCustomLoadMoreView();
                if (null == footerView) {
                    return;
                }
                mFooterCallBack = (IFooterCallBack) footerView;
                if (mFooterCallBack != null) {
                    mFooterCallBack.onStateReady();
                    mFooterCallBack.callWhenNotAutoLoadMore(parent);
                    if (parent != null && !parent.getPullLoadEnable()) {
                        mFooterCallBack.show(false);
                    }
                }
            }
        }
    }

    private void doSilenceLoadMore(BaseRecyclerAdapter adapter, RecyclerView.LayoutManager layoutManager) {
        if (!mIsLoadingMore && isOnRecyclerViewBottom() && !hasLoadCompleted()) {
            if (mRefreshViewListener != null) {
                mIsLoadingMore = true;
                mRefreshViewListener.onLoadMore(true);
            }
        }
    }

    private void doAutoLoadMore(BaseRecyclerAdapter adapter, RecyclerView.LayoutManager layoutManager) {
        if (!mIsLoadingMore && isOnRecyclerViewBottom() && mHideFooter) {
            startLoadMore(false, adapter, layoutManager);
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
            if (!hasLoadCompleted()) {
                doReadyState();
            } else {
                loadCompleted();
            }
        } else {
            setState(XRefreshViewState.STATE_NORMAL);
        }
    }

    public void startLoadMore(boolean silence, BaseRecyclerAdapter adapter, RecyclerView.LayoutManager layoutManager) {
        if (!isFooterEnable() || mIsLoadingMore || mFooterCallBack == null) {
            return;
        }
        if (!hasLoadCompleted()) {
            mIsLoadingMore = true;
            previousTotal = mTotalItemCount;
            mFooterCallBack.onStateRefreshing();
            setState(XRefreshViewState.STATE_LOADING);
            if (mRefreshViewListener != null) {
                mRefreshViewListener.onLoadMore(silence);
            }
        } else {
            loadCompleted();
        }
    }

    public void notifyRecyclerViewLoadMore() {
        if (!mIsLoadingMore) {
            if (!hasLoadCompleted()) {
                if (mRefreshViewListener != null) {
                    mRefreshViewListener.onLoadMore(false);
                }
                mIsLoadingMore = true;
                previousTotal = mTotalItemCount;
                mFooterCallBack.onStateRefreshing();
                setState(XRefreshViewState.STATE_LOADING);
            } else {
                loadCompleted();
            }
        }
    }

    public void releaseToLoadMore(boolean loadmore) {
        if (mFooterCallBack == null || mIsLoadingMore) {
            return;
        }
        if (loadmore) {
            if (mState != XRefreshViewState.STATE_RELEASE_TO_LOADMORE && !addingFooter) {
                mFooterCallBack.onReleaseToLoadMore();
                setState(XRefreshViewState.STATE_RELEASE_TO_LOADMORE);
            }
        } else {
            if (mHideFooter) {
                doReadyState();
            } else {
                if (mState != XRefreshViewState.STATE_READY) {
                    mFooterCallBack.onStateFinish(false);
                    setState(XRefreshViewState.STATE_READY);
                }
            }
        }
    }

    private void doReadyState() {
        if (mState != XRefreshViewState.STATE_READY && !addingFooter) {
            mFooterCallBack.onStateReady();
            setState(XRefreshViewState.STATE_READY);
        }
    }

    public void notifyDatasetChanged() {
        if (isRecyclerView()) {
            final BaseRecyclerAdapter adapter = getRecyclerViewAdapter((RecyclerView) child);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 数据是否满一屏
     *
     * @return
     */
    private boolean onRecyclerViewTop() {
        if (isTop() && mFooterCallBack != null && isFooterEnable()) {
            return true;
        }
        return false;
    }

    private boolean mHideFooter = true;
    private boolean addingFooter = false;

    public void setLoadComplete(boolean hasComplete) {
        mHasLoadComplete = hasComplete;
        if (!hasComplete) {
            mState = XRefreshViewState.STATE_NORMAL;
        }
        mIsLoadingMore = false;
        hasIntercepted = false;

        if (!hasComplete && isHideFooterWhenComplete && mParent != null && mParent.getPullLoadEnable()) {
            addFooterView(true);
        }
        resetLayout();
        if (isRecyclerView()) {
            doRecyclerViewloadComplete(hasComplete);
        }
    }

    private void doRecyclerViewloadComplete(boolean hasComplete) {
        if (mFooterCallBack == null || !isFooterEnable()) return;
        final RecyclerView recyclerView = (RecyclerView) child;
        if (hasComplete) {
            mHideFooter = true;
            mFooterCallBack.onStateFinish(true);
            if (!Utils.isRecyclerViewFullscreen(recyclerView)) {
                child.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadCompleted();
                    }
                }, 200);
            } else {
                int preTotalCount = mTotalItemCount;
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                getRecyclerViewInfo(layoutManager);

                BaseRecyclerAdapter adapter = getRecyclerViewAdapter(recyclerView);
                if (adapter != null) {
                    onRecyclerViewScrolled(recyclerView, adapter, 0, 0, true);
                }
            }
        } else {
            if (recyclerView != null && mFooterCallBack != null) {
                if (!Utils.isRecyclerViewFullscreen(recyclerView)) {
                    mFooterCallBack.onStateReady();
                    mFooterCallBack.callWhenNotAutoLoadMore(mParent);
                    if (!mFooterCallBack.isShowing()) {
                        mFooterCallBack.show(true);
                    }
                } else {
                    doReadyState();
                }
            }
        }
    }

    private BaseRecyclerAdapter getRecyclerViewAdapter(RecyclerView recyclerView) {
        if (recyclerView != null) {
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (adapter instanceof BaseRecyclerAdapter) {
                return (BaseRecyclerAdapter) adapter;
            } else {
                LogUtils.w(RECYCLERVIEW_ADAPTER_WARIN);
            }
        }
        return null;
    }

    public void stopLoading(final boolean hideFooter) {
        mIsLoadingMore = false;
//        mTotalItemCount = 0;
        if (mFooterCallBack != null) {
            mFooterCallBack.onStateFinish(hideFooter);
            if (hideFooter) {
                if (isRecyclerView()) {
                    final RecyclerView recyclerView = (RecyclerView) child;
                    final BaseRecyclerAdapter adapter = (BaseRecyclerAdapter) recyclerView.getAdapter();
                    if (adapter == null) return;
                    addFooterView(false);
                    resetLayout();
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
//        return isBottom();
    }

    public void ensureFooterShowWhenScrolling() {
        if (isFooterEnable() && mFooterCallBack != null && !mFooterCallBack.isShowing()) {
            mFooterCallBack.show(true);
        }
    }

    private void refreshAdapter(BaseRecyclerAdapter adapter, RecyclerView.LayoutManager manager) {
        if (false && adapter != null && manager != null && !mRefreshAdapter && !hasLoadCompleted()) {
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
        mParent.enablePullUp(true);
        if (mState != XRefreshViewState.STATE_COMPLETE) {
            mFooterCallBack.onStateComplete();
            setState(XRefreshViewState.STATE_COMPLETE);
            mPinnedTime = mPinnedTime < 1000 ? 1000 : mPinnedTime;
            if (isHideFooterWhenComplete) {
                child.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        resetLayout();
                        if (mHasLoadComplete) {
                            addFooterView(false);
                        }
                    }
                }, mPinnedTime);
            }
        }
    }

    private void resetLayout() {
        if (mParent != null) {
            mParent.resetLayout();
        }
    }

    private void setState(XRefreshViewState state) {
        if (mState != XRefreshViewState.STATE_COMPLETE) {
            mState = state;
        }
    }

    public XRefreshViewState getState() {
        return mState;
    }

    public boolean hasLoadCompleted() {
        return mHasLoadComplete;
    }

    private void addFooterView(boolean add) {
        if (!(child instanceof RecyclerView)) {
            if (mFooterCallBack != null) {
                mFooterCallBack.show(add);
            }
            return;
        }
        final RecyclerView recyclerView = (RecyclerView) child;
        final BaseRecyclerAdapter adapter = getRecyclerViewAdapter(recyclerView);
        if (adapter != null && mFooterCallBack != null) {
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
//        mTotalItemCount = 0;
        if (enablePullLoad) {
            dealRecyclerViewNotFullScreen();
        }
        if (isRecyclerView()) {
            BaseRecyclerAdapter adapter = getRecyclerViewAdapter((RecyclerView) child);
            if (adapter != null) {
                adapter.insideEnableFooter(enablePullLoad);
            }
        }
    }

    private void dealRecyclerViewNotFullScreen() {
        RecyclerView recyclerView = (RecyclerView) child;
        if (onRecyclerViewTop() && !Utils.isRecyclerViewFullscreen(recyclerView) && child instanceof RecyclerView && mFooterCallBack != null && isFooterEnable()) {
            mFooterCallBack.onStateReady();
            mFooterCallBack.callWhenNotAutoLoadMore(mParent);
            if (!mFooterCallBack.isShowing()) {
                mFooterCallBack.show(true);
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

    private boolean isForbidLoadMore = true;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        /******解决abslistview数据不满一屏的时候，会重复加载更多的问题 start ******/
        if (mParent.isStopLoadMore() && scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            isForbidLoadMore = true;
        }
        if (isForbidLoadMore) {
            if (!mParent.isStopLoadMore() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                isForbidLoadMore = false;
            }
            return;
        }
        /******解决abslistview数据不满一屏的时候，会重复加载更多的问题 end ******/
        if (mSilenceLoadMore) {
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
        if (mSilenceLoadMore) {
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
            if (webview instanceof XWebView) {
                return !((XWebView) webview).isBottom();
            } else {
                float left = webview.getContentHeight() * webview.getScale();
                int right = webview.getHeight() + webview.getScrollY();
                return left != right;
            }
        } else if (child instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) child;
            View childView = scrollView.getChildAt(0);
            if (childView != null) {
                return canScrollVertically(child, 1)
                        || scrollView.getScrollY() < childView.getHeight() - scrollView.getHeight();
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
        if (mSilenceLoadMore) {
            return false;
        } else if (null != child && child instanceof RecyclerView) {
            final RecyclerView recyclerView = (RecyclerView) child;
            if (recyclerView.getAdapter() != null && !(recyclerView.getAdapter() instanceof BaseRecyclerAdapter)) {
                return false;
            }
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