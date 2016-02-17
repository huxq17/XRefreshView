package com.andview.example.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * Created by huxq17 on 2016/2/1.
 */
public class BannerViewPager extends ViewPager {
    public BannerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BannerViewPager(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(ev);
    }

    private ViewGroup parent;

    public void setParent(ViewGroup parent) {
        this.parent = parent;
    }

}
