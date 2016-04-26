package com.andview.refreshview.callback;

import com.andview.refreshview.XRefreshView.XRefreshViewListener;

public interface IFooterCallBack {
    /**
     * 当不是到达底部自动加载更多的时候，需要自己写点击事件
     *
     * @param xRefreshViewListener
     */
    public void callWhenNotAutoLoadMore(XRefreshViewListener xRefreshViewListener);

    /**
     * 正常状态，例如需要点击footerview才能加载更多，主要是到达底部不自动加载更多时会被调用
     */
    public void onStateReady();

    /**
     * 正在刷新
     */
    public void onStateRefreshing();

    /**
     * 刷新结束 在此方法中不要调用show()方法
     * @param hidefooter footerview是否被隐藏
     */
    public void onStateFinish(boolean hidefooter);

    /**
     * 已无更多数据 在此方法中不要调用show()方法
     */
    public void onStateComplete();


    /**
     * 设置显示或者隐藏footerview 不要在onStateFinish和onStateComplete中调用此方法
     * @param show
     */
    public void show(boolean show);

    /**
     * footerview是否显示中
     *
     * @return
     */
    public boolean isShowing();

    /**
     * 获得footerview的高度
     *
     * @return
     */
    public int getFooterHeight();
}
