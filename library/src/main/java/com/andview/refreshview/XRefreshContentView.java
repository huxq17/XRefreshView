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
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

import com.andview.refreshview.XScrollView.OnScrollBottomListener;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.andview.refreshview.callback.IFooterCallBack;
import com.andview.refreshview.listener.OnBottomLoadMoreTime;
import com.andview.refreshview.listener.OnTopRefreshTime;
import com.andview.refreshview.recyclerview.BaseRecyclerAdapter;
import com.andview.refreshview.recyclerview.XSpanSizeLookup;
import com.andview.refreshview.utils.LogUtils;

public class XRefreshContentView implements OnScrollListener, OnTopRefreshTime,
        OnBottomLoadMoreTime {
    private View child;
    // total list items, used to detect is at the bottom of listview.
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

    public void setScrollListener() {
        if (child instanceof AbsListView) {
            AbsListView absListView = (AbsListView) child;
            absListView.setOnScrollListener(this);
        } else if (child instanceof ScrollView) {
            if (child instanceof XScrollView) {
                XScrollView scrollView = (XScrollView) child;
                scrollView
                        .registerOnBottomListener(new OnScrollBottomListener() {

                            @Override
                            public void srollToBottom() {
                                if (mSlienceLoadMore) {
                                    if (mRefreshViewListener != null) {
                                        mRefreshViewListener.onLoadMore(true);
                                    }
                                } else if (mContainer != null
                                        && !mContainer.hasLoadCompleted()) {
                                    mContainer.invokeLoadMore();
                                }
                            }
                        });
            } else {
                throw new RuntimeException(
                        "please use XScrollView instead of ScrollView!");
            }

        } else if (child instanceof RecyclerView) {
            final RecyclerView recyclerView = (RecyclerView) child;
            if (recyclerView.getAdapter() == null) {
                return;
            }
            if (!(recyclerView.getAdapter() instanceof BaseRecyclerAdapter)) {
                throw new RuntimeException("Recylerview的adapter请继承 BaseRecyclerAdapter");
            }
            final BaseRecyclerAdapter adapter = (BaseRecyclerAdapter) recyclerView
                    .getAdapter();
            recyclerView.removeOnScrollListener(mOnScrollListener);

            mOnScrollListener = new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView,
                                                 int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (mRecyclerViewScrollListener != null) {
                        mRecyclerViewScrollListener.onScrollStateChanged(
                                recyclerView, newState);
                    }
                    refreshAdapter(adapter, null);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (mRecyclerViewScrollListener != null) {
                        mRecyclerViewScrollListener.onScrolled(recyclerView,
                                dx, dy);
                    }
                    if (mFooterCallBack == null) {
                        return;
                    }
                    RecyclerView.LayoutManager layoutManager = null;
                    if (layoutManager == null) {
                        layoutManager = recyclerView.getLayoutManager();
                    }
                    getRecyclerViewInfo(layoutManager, adapter);
                    // if (mIsLoadingMore) {
                    // if (mTotalItemCount > previousTotal) {
                    // mIsLoadingMore = false;
                    // previousTotal = mTotalItemCount;
                    // }
                    // }
                    LogUtils.d("pre onLoadMore mIsLoadingMore="
                            + mIsLoadingMore);
                    if (mSlienceLoadMore) {
                        if (!mIsLoadingMore
                                && (mTotalItemCount - 1 - mPreLoadCount) <= mLastVisibleItemPosition) {
                            if (mRefreshViewListener != null) {
                                LogUtils.i("scroll onLoadMore mIsLoadingMore="
                                        + mIsLoadingMore);
                                mIsLoadingMore = true;
                                refreshAdapter(adapter, layoutManager);
                                mRefreshViewListener.onLoadMore(true);
                            }
                        }
                    } else {
                        if (mContainer != null) {
                            if (!mIsLoadingMore
                                    && (mTotalItemCount - 1 - mPreLoadCount) <= mLastVisibleItemPosition) {
                                if (!mContainer.hasLoadCompleted()) {
                                    if (mRefreshViewListener != null) {
                                        refreshAdapter(adapter, layoutManager);
                                        mRefreshViewListener.onLoadMore(false);
                                    }
                                    mIsLoadingMore = true;
                                    previousTotal = mTotalItemCount;
                                    mFooterCallBack.onStateRefreshing();
                                    mState = XRefreshViewState.STATE_LOADING;
                                } else {
                                    loadCompleted();
                                }
                            } else {
                                mState = XRefreshViewState.STATE_NORMAL;
                            }
                        } else if (null == mContainer) {
                            if (!mIsLoadingMore
                                    && (mTotalItemCount - 1 - mPreLoadCount) <= mLastVisibleItemPosition) {
                                refreshAdapter(adapter, layoutManager);
                                if (!mHasLoadComplete) {
                                    if (mState != XRefreshViewState.STATE_READY) {
                                        mFooterCallBack.onStateReady();
                                        mState = XRefreshViewState.STATE_READY;
                                    }
                                } else {
                                    loadCompleted();
                                }
                            } else {
                                mState = XRefreshViewState.STATE_NORMAL;
                            }
                        }
                    }
                }
            };

            recyclerView.addOnScrollListener(mOnScrollListener);
            if (mSlienceLoadMore) {
                return;
            }
            if (adapter != null) {
                View footerView = adapter.getCustomLoadMoreView();
                if (null == footerView) {
                    return;
                }
                adapter.notifyDataSetChanged();
                mFooterCallBack = (IFooterCallBack) footerView;
                // 如果设置到达底部不自动加载更多，那么就点击footerview加载更多
                if (null == mContainer) {
                    mFooterCallBack
                            .callWhenNotAutoLoadMore(mRefreshViewListener);
                }
            }
        }
    }

    private boolean mRefreshAdapter = false;

    private void refreshAdapter(BaseRecyclerAdapter adapter, RecyclerView.LayoutManager manager) {
        if (adapter != null && !mRefreshAdapter) {
            if (!(manager instanceof GridLayoutManager)) {
                View footerView = adapter.getFooterView();
                if (footerView != null) {
                    ViewGroup.LayoutParams layoutParams = footerView.getLayoutParams();
                    if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                        ((StaggeredGridLayoutManager.LayoutParams) layoutParams).setFullSpan(true);
                        mRefreshAdapter = true;
                    }
                }
            }
        }
    }

    public void getRecyclerViewInfo(RecyclerView.LayoutManager layoutManager, BaseRecyclerAdapter adapter) {
        int[] lastPositions = null;
        if (layoutManagerType == null) {
            if (layoutManager instanceof GridLayoutManager) {
                layoutManagerType = LAYOUT_MANAGER_TYPE.GRID;
                GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                GridLayoutManager.SpanSizeLookup lookup = gridLayoutManager.getSpanSizeLookup();
                if (lookup==null || !(lookup instanceof XSpanSizeLookup)) {
                    gridLayoutManager.setSpanSizeLookup(new XSpanSizeLookup(adapter, gridLayoutManager.getSpanCount()));
                }
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
                mLastVisibleItemPosition = ((LinearLayoutManager) layoutManager)
                        .findLastVisibleItemPosition();
            case GRID:
                mLastVisibleItemPosition = ((LinearLayoutManager) layoutManager)
                        .findLastVisibleItemPosition();
                mFirstVisibleItem = ((LinearLayoutManager) layoutManager)
                        .findFirstVisibleItemPosition();
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

    public void loadCompleted() {
        if (mState != XRefreshViewState.STATE_COMPLETE) {
            mFooterCallBack.onStateComplete();
            mState = XRefreshViewState.STATE_COMPLETE;
            mPinnedTime = mPinnedTime < 1000 ? 1000 : mPinnedTime;
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mFooterCallBack.hide();
                }
            }, mPinnedTime);
        }
    }

    public void setLoadComplete(boolean hasComplete) {
        mHasLoadComplete = hasComplete;
        if (!hasComplete) {
            mFooterCallBack.show();
            mIsLoadingMore = false;
        }
    }

    public void setPinnedTime(int pinnedTime) {
        mPinnedTime = pinnedTime;
    }

    public void setOnAbsListViewScrollListener(OnScrollListener listener) {
        mAbsListViewScrollListener = listener;
    }

    public void setOnRecyclerViewScrollListener(
            RecyclerView.OnScrollListener listener) {
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
            if (mRefreshViewListener != null && !mContainer.hasLoadCompleted() && !mIsLoadingMore && mTotalItemCount - 1 <= view.getLastVisiblePosition() + mPreLoadCount) {
                mRefreshViewListener.onLoadMore(true);
                mIsLoadingMore = true;
            }
        } else if (mContainer != null && !mContainer.hasLoadCompleted()
                && scrollState == OnScrollListener.SCROLL_STATE_IDLE
                && mTotalItemCount - 1 <= view.getLastVisiblePosition() + mPreLoadCount) {
            if (!mIsLoadingMore) {
                mIsLoadingMore = mContainer.invokeLoadMore();
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
            mAbsListViewScrollListener.onScroll(view, firstVisibleItem,
                    visibleItemCount, totalItemCount);
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

    public void stopLoading() {
        mIsLoadingMore = false;
        mTotalItemCount = 0;
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

    public static enum LAYOUT_MANAGER_TYPE {
        LINEAR, GRID, STAGGERED_GRID
    }

}
