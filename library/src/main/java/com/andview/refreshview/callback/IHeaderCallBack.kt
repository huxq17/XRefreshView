package com.andview.refreshview.callback

/**
 * 提供自定义headerview的接口
 *
 * @author huxq17@163.com
 */
interface IHeaderCallBack {
    /**
     * 正常状态
     */
    fun onStateNormal()

    /**
     * 准备刷新
     */
    fun onStateReady()

    /**
     * 正在刷新
     */
    fun onStateRefreshing()

    /**
     * 刷新结束
     *
     * @param success 是否刷新成功 success参数由XRefreshView.stopRefresh(boolean)传入
     */
    fun onStateFinish(success: Boolean)

    /**
     * 获取headerview显示的高度与headerview高度的比例
     *
     * @param headerMovePercent  移动距离和headerview高度的比例
     * @param offsetY headerview移动的距离
     */
    fun onHeaderMove(headerMovePercent: Double, offsetY: Int, deltaY: Int)

    /**
     * 设置显示上一次刷新的时间
     *
     * @param lastRefreshTime 上一次刷新的时间
     */
    fun setRefreshTime(lastRefreshTime: Long)

    /**
     * 隐藏footerview
     */
    fun hide()

    /**
     * 显示footerview
     */
    fun show()

    /**
     * 获得headerview的高度,如果不想headerview全部被隐藏，就可以只返回一部分的高度
     *
     * @return
     */
    val headerHeight: Int
}