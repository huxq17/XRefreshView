package com.andview.refreshview.callback;

/**
 * 提供自定义headerview的接口
 * 
 * @author huxq17@163.com
 * 
 */
public interface IHeaderCallBack {
	/**
	 * 正常状态
	 */
	public void onStateNormal();

	/**
	 * 准备刷新
	 */
	public void onStateReady();

	/**
	 * 正在刷新
	 */
	public void onStateRefreshing();

	/**
	 * 刷新结束
	 */
	public void onStateFinish();

	/**
	 * 获取headerview显示的高度与headerview高度的比例
	 * 
	 * @param offset
	 *            移动距离和headerview高度的比例，范围是0~1，0：headerview完全没显示 1：headerview完全显示
	 * @param offsetY
	 *            headerview移动的距离
	 */
	public void onHeaderMove(double offset, int offsetY,int deltaY);

	/**
	 * 设置显示上一次刷新的时间
	 * 
	 * @param lastRefreshTime
	 *            上一次刷新的时间
	 */
	public void setRefreshTime(long lastRefreshTime);

	/**
	 * 隐藏footerview
	 */
	public void hide();

	/**
	 * 显示footerview
	 */
	public void show();

	/**
	 * 获得headerview的高度,如果不想headerview全部被隐藏，就可以只返回一部分的高度
	 * 
	 * @return
	 */
	public int getHeaderHeight();
}