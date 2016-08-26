package com.andview.example.ui.smileyloadingview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.andview.example.R;
import com.andview.refreshview.callback.IHeaderCallBack;
import com.andview.refreshview.utils.LogUtils;

/**
 * Created by 2144 on 2016/8/26.
 */
public class SmileyHeaderView extends LinearLayout implements IHeaderCallBack {
    public SmileyHeaderView(Context context) {
        this(context, null);
    }

    public SmileyHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmileyHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SmileyHeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private SmileyLoadingView loadingView;

    private void init(Context context) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.smiley_headerview, this);
        loadingView = (SmileyLoadingView) contentView.findViewById(R.id.loading_view);
    }

    @Override
    public void onStateNormal() {

    }

    @Override
    public void onStateReady() {
    }

    @Override
    public void onStateRefreshing() {
        loadingView.start(mAngle);
    }

    @Override
    public void onStateFinish(boolean success) {
        loadingView.stop();
    }

    private float mAngle;

    @Override
    public void onHeaderMove(double headerMovePercent, int offsetY, int deltaY) {
        LogUtils.e("onHeaderMove headerMovePercent=" + headerMovePercent + ";offsetY=" + offsetY + ";deltaY=" + deltaY);
        if (headerMovePercent <= 1) {
            mAngle = (float) ((360 + 180 - 90) * headerMovePercent + 90);
            loadingView.smile(mAngle);
        }
    }

    @Override
    public void setRefreshTime(long lastRefreshTime) {

    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    @Override
    public int getHeaderHeight() {
        return getMeasuredHeight();
    }
}
