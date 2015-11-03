package com.andview.refreshview.callback;

import com.andview.refreshview.XRefreshView.XRefreshViewListener;

public interface IFooterCallBack {
	/**
	 * 当不是到达底部自动加载更多的时候，需要自己写点击事件
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
	 * 刷新结束
	 */
	public void onStateFinish();
	/**
	 * 已无更多数据
	 */
	public void onStateComplete();
	/**
	 * 隐藏footerview
	 */
	public void hide();
	/**
	 * 显示footerview
	 */
	public void show();
	/**
	 * 获得footerview的高度
	 * @return
	 */
	public int getFooterHeight();
}
