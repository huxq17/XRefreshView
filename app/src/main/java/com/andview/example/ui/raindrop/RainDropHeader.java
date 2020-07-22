package com.andview.example.ui.raindrop;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.andview.example.DensityUtil;
import com.andview.example.R;
import com.andview.example.Utils;
import com.andview.refreshview.callback.IHeaderCallBack;
import com.andview.refreshview.utils.LogUtils;

/**
 * Created by Administrator on 2015/8/24.
 */
public class RainDropHeader extends FrameLayout implements IHeaderCallBack{
    private RainDropView mRainDropView;
    private LinearLayout mContainer;
    private ProgressBar mProgressBar;

    private int stretchHeight;
    private  int readyHeight;
    private int DISTANCE_BETWEEN_STRETCH_READY = 250;
    public RainDropHeader(Context context) {
        super(context);
        initView();
    }

    public RainDropHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RainDropHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        DISTANCE_BETWEEN_STRETCH_READY= DensityUtil.dip2px(getContext(), DISTANCE_BETWEEN_STRETCH_READY);
        mContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(
                R.layout.waterdroplistview_header, null);
        mProgressBar = (ProgressBar) mContainer.findViewById(R.id.waterdroplist_header_progressbar);
        mRainDropView = (RainDropView) mContainer.findViewById(R.id.waterdroplist_waterdrop);
        // 初始情况，设置下拉刷新view高度为0
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 0);
        addView(mContainer, lp);
//        setBackgroundColor(Color.BLACK);
        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                stretchHeight = mRainDropView.getHeight();
                readyHeight = stretchHeight + DISTANCE_BETWEEN_STRETCH_READY;
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    @Override
    public void onStateNormal() {

    }

    @Override
    public void onStateReady() {

    }

    @Override
    public void onStateRefreshing() {

    }

    @Override
    public void onStateFinish(boolean success) {

    }
    public int getVisiableHeight() {
        return mContainer.getHeight();
    }
    @Override
    public void onHeaderMove(double offset, int offsetY,int deltaY) {
        int height = deltaY+getVisiableHeight();
        setVisiableHeight(height);
        if(height<=stretchHeight)return;
        float pullOffset = (float) Utils.mapValueFromRangeToRange(height, stretchHeight, readyHeight, 0, 1);
        if(pullOffset < 0 || pullOffset >1){
//            throw new IllegalArgumentException("pullOffset should between 0 and 1!"+mState+" "+height);
            return;
        }
        LogUtils.i( "pullOffset:" + pullOffset+";height="+height+";offsetY="+offsetY+";deltaY="+deltaY+";stretchHeight="+stretchHeight+";readyHeight="+readyHeight);
        mRainDropView.updateComleteState(pullOffset);
    }
    public void setVisiableHeight(int height) {
        if (height < 0)
            height = 0;
        LayoutParams lp = (LayoutParams) mContainer
                .getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }
    @Override
    public void setRefreshTime(long lastRefreshTime) {

    }

    @Override
    public void hide() {

    }

    @Override
    public void show() {

    }

    @Override
    public int getHeaderHeight() {
//        stretchHeight =  mRainDropView.getMeasuredHeight();
//        DISTANCE_BETWEEN_STRETCH_READY = DensityUtil.dip2px(getContext(), DISTANCE_BETWEEN_STRETCH_READY);
//        readyHeight = stretchHeight + DISTANCE_BETWEEN_STRETCH_READY;
//        LogUtils.i("getHeaderHeight="+mRainDropView.getMeasuredHeight());
        return mContainer.getHeight();
    }
}
