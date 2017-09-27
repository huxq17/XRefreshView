package com.andview.example.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.andview.example.R;
import com.andview.refreshview.callback.IHeaderCallBack;
import com.andview.refreshview.utils.Utils;

import java.util.Calendar;

public class XRefreshViewWineHeader extends LinearLayout implements IHeaderCallBack {
    private ViewGroup mContent;
    //	private ImageView mArrowImageView;
    private ImageView mOkImageView;
    private View layoutTop;
    //	private ProgressBar mProgressBar;
//	private TextView mHintTextView;
//	private TextView mHeaderTimeTextView;
    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    private final int ROTATE_ANIM_DURATION = 180;

    public XRefreshViewWineHeader(Context context) {
        super(context);
        initView(context, true);
    }

    public XRefreshViewWineHeader(Context context, boolean isAddHeaderTop) {
        super(context);
        initView(context, isAddHeaderTop);
    }

    /**
     * @param context
     * @param attrs
     */
    public XRefreshViewWineHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, true);
    }

    private void initView(Context context, boolean isAddHeaderTop) {
        mContent = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.xrefreshview_wine_header, this);
        mOkImageView = (ImageView) findViewById(R.id.xrefreshview_header_ok);
        layoutTop = findViewById(R.id.layout_top);
        layoutTop.setVisibility(isAddHeaderTop ? VISIBLE : GONE);

        mOkImageView.setImageResource(R.drawable.wine_loading);
        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
    }

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
        mOkImageView.setVisibility(View.VISIBLE);
        ((AnimationDrawable) mOkImageView.getDrawable()).stop();
    }

    @Override
    public void onStateReady() {
        mOkImageView.setVisibility(View.VISIBLE);
        ((AnimationDrawable) mOkImageView.getDrawable()).stop();
    }

    @Override
    public void onStateRefreshing() {
        mOkImageView.setVisibility(View.VISIBLE);
        ((AnimationDrawable) mOkImageView.getDrawable()).start();
    }

    @Override
    public void onStateFinish(boolean success) {
        if (success) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mOkImageView.setVisibility(View.VISIBLE);
                    ((AnimationDrawable) mOkImageView.getDrawable()).stop();
                }
            }, 1000);
        }
    }

    @Override
    public void onHeaderMove(double offset, int offsetY, int deltaY) {

    }

    @Override
    public int getHeaderHeight() {
        return getMeasuredHeight();
    }
}
