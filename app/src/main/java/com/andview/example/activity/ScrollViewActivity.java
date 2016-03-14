package com.andview.example.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andview.example.R;
import com.andview.example.ui.raindrop.CustomerFooter;
import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.SimpleXRefreshListener;

public class ScrollViewActivity extends Activity {
    private XRefreshView outView;
    private LinearLayout ll;
    private int count = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrollview);
        outView = (XRefreshView) findViewById(R.id.custom_view);
        ll = (LinearLayout) findViewById(R.id.ll);
        outView.setAutoRefresh(false);
        outView.setPullLoadEnable(true);
        outView.setPinnedTime(1000);
        outView.setAutoLoadMore(true);
//		outView.setAutoLoadMore(true);
//		outView.setSlienceLoadMore();
        outView.setCustomFooterView(new CustomerFooter(this));

        outView.setXRefreshViewListener(new SimpleXRefreshListener() {

            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        outView.stopRefresh();
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore(boolean isSlience) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //setLoadComplete不要和stopLoadMore同时调用
                        if (count > 100) {
                            outView.setLoadComplete(true);
                        } else {
                            outView.stopLoadMore();
                        }
                        count++;
                    }
                }, 2000);
            }
        });
        for (int i = 0; i < 50; i++) {
            TextView tv = new TextView(this);
            tv.setTextIsSelectable(true);
            tv.setText("数据" + i);
            ll.addView(tv);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 加载菜单
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch (menuId) {
            case R.id.menu_clear:
                outView.setPullLoadEnable(!outView.getPullLoadEnable());
                break;
            case R.id.menu_refresh:
                outView.startRefresh();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
//	@Override
//	protected void onResume() {
//		super.onResume();
//		outView.startRefresh();
//	}
}
