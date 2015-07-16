package com.andview.refreshview.callback;

public interface IFooterCallBack {
	/**
	 * 正在刷新
	 */
	public void onStateRefreshing();
	/**
	 * 刷新结束
	 */
	public void onStateEnd();
	/**
	 * 隐藏footerview
	 */
	public void hide();
	/**
	 * 显示footerview
	 */
	public void show();
}
