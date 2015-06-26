package com.andview.refreshview;

public class XRefreshHolder {
	/**
	 * 在开始上拉加载更多的时候，记录下childView一开始的Y轴坐标
	 */
	public int mOriginChildY = -1;
	/**
	 * 在开始上拉加载更多的时候，记录下FootView一开始的Y轴坐标
	 */
	public int mOriginFootY = -1;
	/**
	 * 在开始上拉加载更多的时候，记录下HeadView一开始的Y轴坐标
	 */
	public int mOriginHeadY = -1;
	public int lastChidY;
	public int lastFootY;
	public int lastHeaderY;

	public int mOffsetY;

	public boolean isHeaderVisible() {
		return lastHeaderY >= mOriginHeadY;
	}

	public boolean isFooterVisible() {
		return lastFootY <= mOriginFootY;
	}

	public void setOriginHeadY(int headY) {
		mOriginHeadY = headY;
		lastHeaderY = mOriginHeadY;
	}

	public void setOriginChildY(int childY) {
		mOriginChildY = childY;
		lastChidY = mOriginChildY;
	}

	public int getCurrentHeadY() {
		return mOffsetY + mOriginHeadY;
	}

	public int getCurrentChildY() {
		return mOffsetY + mOriginChildY;
	}

	public void setLastY() {
		lastHeaderY = mOffsetY + mOriginHeadY;
		lastChidY = mOffsetY + mOriginChildY;
	}
	public void move(int deltaY){
		mOffsetY +=deltaY;
	}
}
