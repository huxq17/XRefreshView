package com.andview.example.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andview.example.R;
import com.andview.refreshview.callback.IHeaderCallBack;

public class CustomGifHeader extends LinearLayout implements IHeaderCallBack {
    private GifView gifView1;
    private GifView gifView2;
    private TextView mHintTextView;

    public CustomGifHeader(Context context) {
        super(context);
        setBackgroundColor(Color.parseColor("#f3f3f3"));
        initView(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public CustomGifHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.gif_header, this);
        gifView1 = (GifView) findViewById(R.id.gif1);
        mHintTextView = (TextView) findViewById(R.id.gif_header_hint);
        gifView2 = (GifView) findViewById(R.id.gif2);
        gifView1.setMovieResource(R.raw.vertical);
        gifView2.setMovieResource(R.raw.horizontal);
        gifView2.setVisibility(View.GONE);
    }

    public void setRefreshTime(long lastRefreshTime) {
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    @Override
    public void onStateNormal() {
        mHintTextView.setText(R.string.xrefreshview_header_hint_normal);
        gifView1.setVisibility(View.VISIBLE);
        gifView2.setVisibility(View.GONE);
        gifView1.setPaused(false);
        gifView2.setPaused(true);
    }

    @Override
    public void onStateReady() {
        mHintTextView.setText(R.string.xrefreshview_header_hint_ready);
    }

    @Override
    public void onStateRefreshing() {
        mHintTextView.setText(R.string.xrefreshview_header_hint_refreshing);
        gifView1.setVisibility(View.GONE);
        gifView2.setVisibility(View.VISIBLE);
        gifView1.setPaused(true);
        gifView2.setPaused(false);
    }

    @Override
    public void onStateFinish(boolean success) {
        mHintTextView.setText(success ? R.string.xrefreshview_header_hint_loaded : R.string.xrefreshview_header_hint_loaded_fail);
//        gifView1.setVisibility(View.VISIBLE);
        gifView2.setVisibility(View.GONE);
        gifView2.setPaused(true);
    }

    @Override
    public void onHeaderMove(double headerMovePercent, int offsetY, int deltaY) {
        //
    }

    @Override
    public int getHeaderHeight() {
        return getMeasuredHeight();
    }
}
