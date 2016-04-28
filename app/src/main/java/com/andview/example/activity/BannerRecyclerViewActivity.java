package com.andview.example.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.andview.example.IndexPageAdapter;
import com.andview.example.R;
import com.andview.example.recylerview.Person;
import com.andview.example.recylerview.SimpleAdapter;
import com.andview.example.ui.AdHeader;
import com.andview.example.ui.BannerViewPager;
import com.andview.example.ui.CustomGifHeader;
import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.SimpleXRefreshListener;
import com.andview.refreshview.XRefreshViewFooter;

import java.util.ArrayList;
import java.util.List;

public class BannerRecyclerViewActivity extends Activity {
    RecyclerView recyclerView;
    SimpleAdapter adapter;
    List<Person> personList = new ArrayList<Person>();
    XRefreshView xRefreshView;
    int lastVisibleItem = 0;
    GridLayoutManager layoutManager;
    private boolean isBottom = false;
    private int mLoadCount = 0;
    private AdHeader adHeader;

    private BannerViewPager mBannerViewPager;
    private int[] mImageIds = new int[]{R.mipmap.test01, R.mipmap.test02,
            R.mipmap.test03};// 测试图片id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recylerview);
        xRefreshView = (XRefreshView) findViewById(R.id.xrefreshview);
        xRefreshView.setPullLoadEnable(true);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_test_rv);
        recyclerView.setHasFixedSize(true);

        initData();
        adapter = new SimpleAdapter(personList, this);
        // 设置静默加载模式
//		xRefreshView.setSlienceLoadMore();
        layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        headerView = adapter.setHeaderView(R.layout.bannerview, recyclerView);
//        LayoutInflater.from(this).inflate(R.layout.bannerview, rootview);
        mBannerViewPager = (BannerViewPager) headerView.findViewById(R.id.index_viewpager);

//        adHeader = new AdHeader(this);
//        mBannerViewPager = (LoopViewPager) adHeader.findViewById(R.id.index_viewpager);
        initViewPager();
        CustomGifHeader header = new CustomGifHeader(this);
        xRefreshView.setCustomHeaderView(header);
        recyclerView.setAdapter(adapter);
//        xRefreshView.setAutoLoadMore(true);
        xRefreshView.setPinnedTime(1000);
        xRefreshView.setMoveForHorizontal(true);
//        adapter.setHeaderView(headerView, recyclerView);
        adapter.setCustomLoadMoreView(new XRefreshViewFooter(this));

//		xRefreshView.setPullLoadEnable(false);
        //设置静默加载时提前加载的item个数
//		xRefreshView.setPreLoadCount(2);

        xRefreshView.setXRefreshViewListener(new SimpleXRefreshListener() {

            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        xRefreshView.stopRefresh();
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore(boolean isSlience) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
//                        for (int i = 0; i < 6; i++) {
//                            adapter.insert(new Person("More ", "21"),
//                                    adapter.getAdapterItemCount());
//                        }
//                        mLoadCount++;
                        if (mLoadCount >= 3) {
                            xRefreshView.setLoadComplete(true);
                        } else {
                            // 刷新完成必须调用此方法停止加载
                            xRefreshView.stopLoadMore(false);
                        }
                    }
                }, 1000);
            }
        });
    }

    private void initData() {
        for (int i = 0; i < 30; i++) {
            Person person = new Person("name" + i, "" + i);
            personList.add(person);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private View headerView;

    private void initViewPager() {
        IndexPageAdapter pageAdapter = new IndexPageAdapter(this, mImageIds);
        mBannerViewPager.setAdapter(pageAdapter);
        mBannerViewPager.setParent(recyclerView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch (menuId) {
            case R.id.menu_clear:
                mLoadCount = 0;
                xRefreshView.setLoadComplete(false);
                break;
            case R.id.menu_refresh:
                xRefreshView.startRefresh();
//                xRefreshView.setPullRefreshEnable(true);
//                xRefreshView.setPullLoadEnable(!xRefreshView.getPullLoadEnable());
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}