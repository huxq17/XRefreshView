package com.andview.example.activity;

import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;

import com.andview.example.DensityUtil;
import com.andview.example.R;
import com.andview.example.view.XRefreshViewWineHeader;
import com.andview.refreshview.XRefreshView;

public class WineActivity extends Activity {
    private XRefreshView wineXRefreshView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wine);

        initView();
        initData();
    }

    private void initView() {
        wineXRefreshView = (XRefreshView) findViewById(R.id.wineXRefreshView);
        wineXRefreshView.setCustomHeaderView(new XRefreshViewWineHeader(this));
        wineXRefreshView.setAddHeaderTop(true);
        wineXRefreshView.setHeaderTopHeight(DensityUtil.dip2px(this, 212));
    }

    private void initData() {
        wineXRefreshView.setAutoRefresh(false);
        wineXRefreshView.setPullRefreshEnable(true);
        wineXRefreshView.setMoveForHorizontal(true);
        wineXRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onRefresh(boolean isPullDown) {
                super.onRefresh(isPullDown);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        wineXRefreshView.stopRefresh();
                    }
                }, 1000);
            }
        });
    }

}
