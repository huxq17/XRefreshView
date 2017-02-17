package com.andview.example.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.andview.example.R;
import com.andview.example.StickyListBean;
import com.andview.example.StickylistAdapter;
import com.andview.example.stickyListHeaders.StickyListHeadersListView;
import com.andview.example.ui.raindrop.CustomerFooter;
import com.andview.example.ui.smileyloadingview.SmileyHeaderView;
import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.listener.OnBottomLoadMoreTime;
import com.andview.refreshview.listener.OnTopRefreshTime;

import java.util.ArrayList;
import java.util.List;

public class SmileViewActivity extends Activity {
    private StickyListHeadersListView stickyLv;
    private List<StickyListBean> list = new ArrayList<StickyListBean>();
    private XRefreshView refreshView;
    private int mTotalItemCount;
    private StickylistAdapter adapter;
    private final int mPinnedTime = 1000;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customview);
        initData();
        stickyLv = (StickyListHeadersListView) findViewById(R.id.sticky_list);
        adapter = new StickylistAdapter(getApplicationContext(), list);
        stickyLv.setAdapter(adapter);
        refreshView = (XRefreshView) findViewById(R.id.custom_view);
        refreshView.setPullLoadEnable(true);
        refreshView.setPinnedTime(mPinnedTime);
        refreshView.setCustomHeaderView(new SmileyHeaderView(this));
        refreshView.setCustomFooterView(new CustomerFooter(SmileViewActivity.this));
        refreshView.setOnTopRefreshTime(new OnTopRefreshTime() {

            @Override
            public boolean isTop() {
                if (stickyLv.getFirstVisiblePosition() == 0) {
                    View firstVisibleChild = stickyLv.getListChildAt(0);
                    return firstVisibleChild.getTop() >= 0;
                }
                return false;
            }
        });
        refreshView.setOnBottomLoadMoreTime(new OnBottomLoadMoreTime() {

            @Override
            public boolean isBottom() {
                if (stickyLv.getLastVisiblePosition() == mTotalItemCount - 1) {
                    View lastChild = stickyLv.getListChildAt(stickyLv.getListChildCount() - 1);
                    return (lastChild.getBottom() + stickyLv.getPaddingBottom()) <= stickyLv.getMeasuredHeight();
                }
                return false;
            }
        });
        stickyLv.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                mTotalItemCount = totalItemCount;
            }
        });
        refreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {

            @Override
            public void onRefresh(boolean isPullDown) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshView.stopRefresh();
                    }
                }, 4000);
            }

            @Override
            public void onLoadMore(boolean isSilence) {

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        refreshView.stopLoadMore();
                    }
                }, 2000);
            }
        });
    }

    int section = 0;
    String YM = null;
    String content = null;

    private void initData() {

        for (int i = 0; i < 20; i++) {
            if (i % 5 == 0) {
                section++;
                YM = "第" + section + "个头";
            }
            content = "第" + i + "项数据";
            StickyListBean bean = new StickyListBean(section, YM, content);
            list.add(bean);
        }

    }
}
