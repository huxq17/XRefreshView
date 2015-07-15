package com.andview.refreshview.base;

import com.andview.refreshview.XRefreshViewState;

public interface XRefreshHeaderViewBase {
	public void setState(XRefreshViewState state);
	public void setRefreshTime(long lastRefreshTime);
	public void hide();
	public void show();
}
