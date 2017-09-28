package com.andview.example.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.andview.example.R;
import com.andview.example.view.XRefreshViewJDHeader;
import com.andview.example.view.XRefreshViewWineHeader;
import com.andview.refreshview.XRefreshView;

public class JDActivity extends Activity {
    private final static String TAG = JDActivity.class.getSimpleName();

    private ImageView ivPerson;
    private ImageView ivPackage;

    private Button btnPackageScan;
    private Button btnPersonScan;

    private SeekBar jdSeekBar;

    private XRefreshView jdXRefreshView;

    private float startPoint = 0;
    private float endPoint = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jd);
        initView();
        initData();
    }

    private void initView() {
        ivPerson = (ImageView) findViewById(R.id.ivPerson);
        ivPackage = (ImageView) findViewById(R.id.ivPackage);

        btnPackageScan = (Button) findViewById(R.id.btnPackageScan);
        btnPersonScan = (Button) findViewById(R.id.btnPersonScan);

        jdSeekBar = (SeekBar) findViewById(R.id.jdSeekBar);

        jdXRefreshView = (XRefreshView) findViewById(R.id.jdXRefreshView);
        jdXRefreshView.setCustomHeaderView(new XRefreshViewJDHeader(this));
    }

    private void initData() {

        btnPackageScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                packageScan();
            }
        });

        btnPersonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personScan();
            }
        });


        jdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                startPoint = endPoint;
                endPoint = (float) progress / 100f;
                if (endPoint != 1f) {
                    ivPackage.setImageResource(R.drawable.jd_package);
                    ivPerson.setImageResource(R.drawable.jd_person);
                }

                ScaleAnimation scaleAnimation = new ScaleAnimation(startPoint, endPoint, startPoint, endPoint, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setFillAfter(true);
                ivPackage.startAnimation(scaleAnimation);

                ScaleAnimation scaleAnimationPerson = new ScaleAnimation(startPoint, endPoint, startPoint, endPoint, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimationPerson.setFillAfter(true);
                ivPerson.startAnimation(scaleAnimationPerson);

                if (startPoint != endPoint) {
                    startPoint = endPoint;
                }

                if (endPoint == 1f) {
                    ivPackage.setImageDrawable(null);
                    ivPerson.setImageResource(R.drawable.jd_loading);
                    ((AnimationDrawable) ivPerson.getDrawable()).start();

                }

                Log.d(TAG, "滑动初始距离：" + startPoint);
                Log.d(TAG, "滑动结束比例：" + endPoint);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        jdXRefreshView.setAutoRefresh(false);
        jdXRefreshView.setPullRefreshEnable(true);
        jdXRefreshView.setMoveForHorizontal(true);
        jdXRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onRefresh(boolean isPullDown) {
                super.onRefresh(isPullDown);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        jdXRefreshView.stopRefresh();
                    }
                }, 1000);
            }
        });
    }

    private void packageScan() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(1000);
        ivPackage.startAnimation(scaleAnimation);
    }

    private void personScan() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(1000);
        ivPerson.startAnimation(scaleAnimation);
    }


}
