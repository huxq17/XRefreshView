package com.andview.example.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.andview.example.DensityUtil;
import com.andview.example.R;
import com.andview.refreshview.callback.IHeaderCallBack;
import com.andview.refreshview.utils.Utils;

import java.util.Calendar;

public class XRefreshViewJDHeader extends LinearLayout implements IHeaderCallBack {
    private ViewGroup mContent;

    private ImageView ivPerson;
    private ImageView ivPackage;

    private float startPoint = 0;
    private float endPoint = 0;

    public XRefreshViewJDHeader(Context context) {
        super(context);
        initView(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public XRefreshViewJDHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mContent = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.xrefreshview_jd_header, this);
        ivPackage = (ImageView) mContent.findViewById(R.id.ivPackage);
        ivPerson = (ImageView) mContent.findViewById(R.id.ivPerson);
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
        ivPackage.setImageResource(R.drawable.jd_package);
        ivPerson.setImageResource(R.drawable.jd_person);
    }

    @Override
    public void onStateReady() {
        ivPackage.setImageResource(R.drawable.jd_package);
        ivPerson.setImageResource(R.drawable.jd_person);
    }

    @Override
    public void onStateRefreshing() {
        ivPackage.setImageDrawable(null);
        ivPerson.setImageResource(R.drawable.jd_loading);
        ((AnimationDrawable) ivPerson.getDrawable()).start();
    }

    @Override
    public void onStateFinish(boolean success) {
        if (success) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivPackage.setImageResource(R.drawable.jd_package);
                    ivPerson.setImageResource(R.drawable.jd_person);
                }
            }, 1000);
        }
    }

    @Override
    public void onHeaderMove(double offset, int offsetY, int deltaY) {
        Log.d("offsetY", "y轴滑动距离 offsetY：" + offsetY + "对应的距离73dp:" + DensityUtil.dip2px(getContext(), 73));

        startPoint = endPoint;
        endPoint = (float) offsetY / (float) DensityUtil.dip2px(getContext(), 73);

        if (endPoint <= 1) {

            ScaleAnimation scaleAnimation = new ScaleAnimation(startPoint, endPoint, startPoint, endPoint, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setFillAfter(true);
            ivPackage.startAnimation(scaleAnimation);

            ScaleAnimation scaleAnimationPerson = new ScaleAnimation(startPoint, endPoint, startPoint, endPoint, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimationPerson.setFillAfter(true);
            ivPerson.startAnimation(scaleAnimationPerson);
        }

        if (startPoint != endPoint) {
            startPoint = endPoint;
        }
    }

    @Override
    public void setRefreshTime(long lastRefreshTime) {
        // 获取当前时间
        Calendar mCalendar = Calendar.getInstance();
        long refreshTime = mCalendar.getTimeInMillis();
        long howLong = refreshTime - lastRefreshTime;
        int minutes = (int) (howLong / 1000 / 60);
        String refreshTimeText = null;
        Resources resources = getContext().getResources();
        if (minutes < 1) {
            refreshTimeText = resources.getString(R.string.xrefreshview_refresh_justnow);
        } else if (minutes < 60) {
            refreshTimeText = resources.getString(R.string.xrefreshview_refresh_minutes_ago);
            refreshTimeText = Utils.format(refreshTimeText, minutes);
        } else if (minutes < 60 * 24) {
            refreshTimeText = resources.getString(R.string.xrefreshview_refresh_hours_ago);
            refreshTimeText = Utils.format(refreshTimeText, minutes / 60);
        } else {
            refreshTimeText = resources.getString(R.string.xrefreshview_refresh_days_ago);
            refreshTimeText = Utils.format(refreshTimeText, minutes / 60 / 24);
        }
    }

    @Override
    public int getHeaderHeight() {
        return getMeasuredHeight();
    }
}
