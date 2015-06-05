package com.andview.refreshview;

public interface XRefreshHeaderViewBase {
	/**
	 * 返回header内容的高度
	 * @return
	 */
	public int getHeaderContentHeight();
	public void setState(XRefreshViewState state);
	public void setRefreshTime(String time);
}
