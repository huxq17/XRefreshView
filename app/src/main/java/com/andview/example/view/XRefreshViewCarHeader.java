package com.andview.example.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andview.example.DensityUtil;
import com.andview.example.R;
import com.andview.refreshview.callback.IHeaderCallBack;

/**
 * Author： fanyafeng
 * Date： 18/10/1 下午5:17
 * Email: fanyafeng@live.cn
 */
public class XRefreshViewCarHeader extends LinearLayout implements IHeaderCallBack {
    private ViewGroup mContent;

    private ImageView ivStatus;
    private TextView tvStatus;

    private float startPoint = 0;
    private float endPoint = 0;

    public XRefreshViewCarHeader(Context context) {
        super(context);
        initView(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public XRefreshViewCarHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mContent = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.xrefreshview_car_header, this);
        tvStatus = (TextView) mContent.findViewById(R.id.tvStatus);
        ivStatus = (ImageView) mContent.findViewById(R.id.ivStatus);
    }

    /**
     * hide footer when disable pull load more
     */
    public void hide() {
        setVisibility(View.GONE);
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    @Override
    public void onStateNormal() {
        tvStatus.setText("下拉刷新查看状态");
        ivStatus.setImageResource(R.drawable.colors_refresh_icon);
    }

    @Override
    public void onStateReady() {
        tvStatus.setText("松手后刷新");
        ivStatus.setImageResource(R.drawable.colors_refresh_icon);
    }

    @Override
    public void onStateRefreshing() {
        tvStatus.setText("正在刷新");
        ivStatus.setImageResource(R.drawable.colors_refresh_loading);
        ((AnimationDrawable) ivStatus.getDrawable()).start();
    }

    @Override
    public void onStateFinish(boolean success) {
        if (success) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvStatus.setText("刷新完成");
                    ivStatus.setImageResource(R.drawable.colors_refresh_icon);
                }
            }, 1000);
        }
    }

    @Override
    public void onHeaderMove(double offset, int offsetY, int deltaY) {
        Log.d("offsetY", "y轴滑动距离 offsetY：" + offsetY + "对应的距离73dp:" + DensityUtil.dip2px(getContext(), 73));
    }

    @Override
    public void setRefreshTime(long lastRefreshTime) {

    }

    @Override
    public int getHeaderHeight() {
        return getMeasuredHeight();
    }
}
