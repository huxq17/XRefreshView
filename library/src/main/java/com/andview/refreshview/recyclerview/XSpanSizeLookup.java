package com.andview.refreshview.recyclerview;

import android.support.v7.widget.GridLayoutManager;

/**
 * use this class to let the footerview have full width
 */
public class XSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    private UltimateViewAdapter adapter;
    private int mSpanSize = 1;

    public XSpanSizeLookup(UltimateViewAdapter adapter, int spanSize) {
        this.adapter = adapter;
        this.mSpanSize = spanSize;
    }

    @Override
    public int getSpanSize(int position) {
        boolean isHeaderOrFooter =  adapter.isFooterShowing(position);
        return isHeaderOrFooter ? mSpanSize : 1;
    }
}