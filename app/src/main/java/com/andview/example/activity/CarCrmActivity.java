package com.andview.example.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.andview.example.R;
import com.andview.example.view.XRefreshViewCarHeader;
import com.andview.refreshview.XRefreshView;

public class CarCrmActivity extends Activity {

    private XRefreshView carCrmXRefreshView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_crm);
        initView();
        initData();
    }

    private void initView() {
        carCrmXRefreshView = (XRefreshView) findViewById(R.id.carCrmXRefreshView);
        carCrmXRefreshView.setCustomHeaderView(new XRefreshViewCarHeader(this));
        carCrmXRefreshView.setMoveForHorizontal(true);
    }

    private void initData() {
        carCrmXRefreshView.setAutoRefresh(false);
        carCrmXRefreshView.setPullRefreshEnable(true);
        carCrmXRefreshView.setMoveForHorizontal(true);
        carCrmXRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onRefresh(boolean isPullDown) {
                super.onRefresh(isPullDown);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        carCrmXRefreshView.stopRefresh();
                    }
                }, 2000);
            }
        });
    }
}
