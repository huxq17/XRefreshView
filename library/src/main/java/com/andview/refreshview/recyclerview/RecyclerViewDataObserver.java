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
    private boolean hasData;

    public RecyclerViewDataObserver() {

    }

    public void setData(BaseRecyclerAdapter adapter, XRefreshView xRefreshView) {
        mAdapter = adapter;
        this.xRefreshView = xRefreshView;
    }

    @Override
    public void onChanged() {
        if (xRefreshView == null || mAdapter == null) {
            return;
        }
        if (mAdapter.getAdapterItemCount() == 0 && mAdapter.getStart() == 0) {
            if (hasData) {
                xRefreshView.enableEmptyView(true);
                hasData = false;
            }
        } else {
            if (!hasData) {
                xRefreshView.enableEmptyView(false);
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
