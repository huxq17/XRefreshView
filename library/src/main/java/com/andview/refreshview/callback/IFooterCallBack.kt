package com.andview.refreshview.callback

import com.andview.refreshview.XRefreshView

interface IFooterCallBack {
    /**
     * 当不是到达底部自动加载更多的时候，需要自己写点击事件
     *
     * @param xRefreshView
     */
    fun callWhenNotAutoLoadMore(xRefreshView: XRefreshView?)

    /**
     * 正常状态，例如需要点击footerview才能加载更多，主要是到达底部不自动加载更多时会被调用
     */
    fun onStateReady()

    /**
     * 正在刷新
     */
    fun onStateRefreshing()

    /**
     * 当footerview被上拉时，松开手指即可加载更多
     */
    fun onReleaseToLoadMore()

    /**
     * 刷新结束 在此方法中不要调用show()方法
     *
     * @param hidefooter footerview是否被隐藏,hideFooter参数由XRefreshView.stopLoadMore(boolean)传入
     */
    fun onStateFinish(hidefooter: Boolean)

    /**
     * 已无更多数据 在此方法中不要调用show()方法
     */
    fun onStateComplete()

    /**
     * 设置显示或者隐藏footerview 不要在onStateFinish和onStateComplete中调用此方法
     *
     * @param show
     */
    fun show(show: Boolean)

    /**
     * footerview是否显示中
     *
     * @return
     */
    val isShowing: Boolean

    /**
     * 获得footerview的高度
     *
     * @return
     */
    val footerHeight: Int
}