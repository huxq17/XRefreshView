package com.andview.refreshview.recyclerview;

import android.support.v7.widget.RecyclerView;

import com.andview.refreshview.XRefreshView;

/**
 * Created by 2144 on 2016/8/16.
 */
public class RecyclerViewDataObserver extends RecyclerView.AdapterDataObserver {
    private BaseRecyclerAdapter mAdapter;
    private XRefreshView xRefreshView;
    private boolean mAttached;
    private boolean hasData = true;

    public RecyclerViewDataObserver() {

    }

    public void setData(BaseRecyclerAdapter adapter, XRefreshView xRefreshView) {
        mAdapter = adapter;
        this.xRefreshView = xRefreshView;
//        onChanged();
    }

    private void enableEmptyView(boolean enable) {
        if (xRefreshView != null) {
            xRefreshView.enableEmptyView(enable);
        }
    }

    @Override
    public void onChanged() {
        if (mAdapter == null) {
            return;
        }
        if (mAdapter.isEmpty()) {
            if (hasData) {
                enableEmptyView(true);
                hasData = false;
            }
        } else {
            if (!hasData) {
                enableEmptyView(false);
                hasData = true;
            }
        }
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        onChanged();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        onChanged();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        onChanged();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        onChanged();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        onChanged();
    }


    public void attach() {
        mAttached = true;
    }

    public boolean hasAttached() {
        return mAttached;
    }
}
