package com.andview.refreshview.base;

import com.andview.refreshview.XRefreshViewState;

public interface XRefreshHeaderViewBase {
	/**
	 * 返回header内容的高度
	 * @return
	 */
	public int getHeaderContentHeight();
	public void setState(XRefreshViewState state);
	public void setRefreshTime(long lastRefreshTime);
}
