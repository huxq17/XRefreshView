package com.andview.refreshview;

public class XRefreshHolder {
	/**
	 * 在开始上拉加载更多的时候，记录下childView一开始的Y轴坐标
	 */
	public float mOriginChildY = -1;
	/**
	 * 在开始上拉加载更多的时候，记录下FootView一开始的Y轴坐标
	 */
	public float mOriginFootY = -1;
	/**
	 * 在开始上拉加载更多的时候，记录下HeadView一开始的Y轴坐标
	 */
	public float mOriginHeadY = -1;
	public float lastChidY;
	public float lastFootY;
	public float lastHeaderY;

	public float mOffsetY;

	public boolean isHeaderVisible() {
		return lastHeaderY >= mOriginHeadY;
	}

	public boolean isFooterVisible() {
		return lastFootY <= mOriginFootY;
	}

	public void setOriginHeadY(float headY) {
		mOriginHeadY = headY;
		lastHeaderY = mOriginHeadY;
	}

	public void setOriginChildY(float childY) {
		mOriginChildY = childY;
		lastChidY = mOriginChildY;
	}

	public float getCurrentHeadY() {
		return mOffsetY + mOriginHeadY;
	}

	public float getCurrentChildY() {
		return mOffsetY + mOriginChildY;
	}

	public void setLastY() {
		lastHeaderY = mOffsetY + mOriginHeadY;
		lastChidY = mOffsetY + mOriginChildY;
	}
}
