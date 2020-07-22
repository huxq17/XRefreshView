package com.andview.example.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.andview.example.IndexPageAdapter;
import com.andview.example.R;
import com.andview.example.ui.AdHeader;
import com.andview.example.ui.BannerViewPager;
import com.andview.example.ui.raindrop.CustomerFooter;
import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.SimpleXRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class HeadAdActivity extends Activity {
    private GridView gv;
    private List<String> str_name = new ArrayList<String>();
    private XRefreshView outView;
    private ArrayAdapter<String> adapter;
    private BannerViewPager mLoopViewPager;
    private AdHeader headerView;
    private int[] mImageIds = new int[]{R.mipmap.test01, R.mipmap.test02,
            R.mipmap.test03};// 测试图片id

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);
        for (int i = 0; i < 50; i++) {
            str_name.add("数据" + i);
        }
        gv = (GridView) findViewById(R.id.gv);
        outView = (XRefreshView) findViewById(R.id.custom_view);

        headerView = new AdHeader(this);
        mLoopViewPager = (BannerViewPager) headerView.findViewById(R.id.index_viewpager);
        initViewPager();

        outView.setPullLoadEnable(true);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, str_name);
        gv.setAdapter(adapter);
        outView.setPinnedTime(1000);
        // outView.setAutoLoadMore(false);
        outView.setCustomHeaderView(headerView);
        outView.setCustomFooterView(new CustomerFooter(this));
        // outView.setCustomHeaderView(new XRefreshViewHeader(this));
        outView.setMoveForHorizontal(true);
//        outView.setPinnedContent(true);
        outView.setXRefreshViewListener(new SimpleXRefreshListener() {
            @Override
            public void onRefresh(boolean isPullDown) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        outView.stopRefresh();
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore(boolean isSilence) {
                final List<String> addlist = new ArrayList<String>();
                for (int i = 0; i < 20; i++) {
                    addlist.add("数据" + (i + str_name.size()));
                }

                new Handler().postDelayed(new Runnable() {

                    @SuppressLint("NewApi")
                    @Override
                    public void run() {
                        if (str_name.size() <= 70) {
                            if (Build.VERSION.SDK_INT >= 11) {
                                str_name.addAll(addlist);
                                adapter.addAll(addlist);
                            }
                            outView.stopLoadMore();
                        } else {
                            outView.setLoadComplete(true);
                        }
                    }
                }, 2000);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        outView.startRefresh();
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
                str_name.clear();
                for (int i = 0; i < 50; i++) {
                    str_name.add("数据" + i);
                }
                adapter.notifyDataSetChanged();
                outView.setLoadComplete(false);
                break;
            case R.id.menu_refresh:
                outView.startRefresh();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViewPager() {
        IndexPageAdapter pageAdapter = new IndexPageAdapter(this, mImageIds);
        mLoopViewPager.setAdapter(pageAdapter);
    }
}
