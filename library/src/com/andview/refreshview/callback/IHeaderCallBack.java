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
	public void onStateEnd();

	/**
	 * 获取headerview显示的高度与headerview高度的比例
	 * 
	 * @param offset
	 *            范围是0~1，0：headerview完全没显示 1：headerview完全显示
	 */
	public void onHeaderMove(double offset);

	public void setRefreshTime(long lastRefreshTime);

	/**
	 * 隐藏footerview
	 */
	public void hide();

	/**
	 * 显示footerview
	 */
	public void show();
}