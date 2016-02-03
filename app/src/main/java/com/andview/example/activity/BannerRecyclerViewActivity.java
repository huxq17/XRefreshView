package com.andview.example.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.andview.example.IndexPageAdapter;
import com.andview.example.R;
import com.andview.example.recylerview.Person;
import com.andview.example.recylerview.SimpleAdapter;
import com.andview.example.ui.AdHeader;
import com.andview.example.ui.GifHeader;
import com.andview.example.ui.LoopViewPager;
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

    private LoopViewPager mLoopViewPager;
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
        adapter = new SimpleAdapter(personList,this);
        // 设置静默加载模式
//		xRefreshView.setSlienceLoadMore();
        layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        headerView = adapter.setHeaderView(R.layout.bannerview, recyclerView);
//        LayoutInflater.from(this).inflate(R.layout.bannerview, rootview);
        mLoopViewPager = (LoopViewPager) headerView.findViewById(R.id.index_viewpager);

//        adHeader = new AdHeader(this);
//        mLoopViewPager = (LoopViewPager) adHeader.findViewById(R.id.index_viewpager);
        initViewPager();
        xRefreshView.setCustomHeaderView(new GifHeader(this));
        recyclerView.setAdapter(adapter);
        xRefreshView.setAutoLoadMore(true);
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
                        for (int i = 0; i < 6; i++) {
                            adapter.insert(new Person("More ", "21"),
                                    adapter.getAdapterItemCount());
                        }
                        mLoadCount++;
                        if (mLoadCount >= 3) {
                            xRefreshView.setLoadComplete(true);
                        } else {
                            // 刷新完成必须调用此方法停止加载
                            xRefreshView.stopLoadMore();
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
        // // 把图片等比例放缩
        // if (Constant.mScreenWidth != 0) {
        // FrameLayout.LayoutParams params =
        // (android.widget.FrameLayout.LayoutParams) mLoopViewPager
        // .getLayoutParams();
        // params.height = (int) (Constant.mScreenWidth * (380.0 / 640.0));
        // mLoopViewPager.setLayoutParams(params);
        // }

        if (mImageIds.length == 1) {
            mLoopViewPager.setPagingEnabled(true);// 如果是一张图片设置不能滑动
        }
        mLoopViewPager.setAdapter(pageAdapter);
        mLoopViewPager.setOnPageChangeListener(new GuidePageChangeListener());

        mLoopViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        // 开始图片滚动
//					if (mScheduledExecutorService != null
//							&& mScheduledExecutorService.isShutdown()) {
//					}
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private int mCurrentItem = 0;
    private UIHandler mUiHandler;

    private class ScrollTask implements Runnable {

        public void run() {
//			synchronized (mLoopViewPager) {
            mCurrentItem = (mCurrentItem + 1) % mImageIds.length;
            Message msg = mUiHandler.obtainMessage();
            msg.what = 1000;
            msg.sendToTarget(); // 通过Handler切换图片
//			}
        }
    }

    private class UIHandler extends Handler {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 1000:
                    mLoopViewPager.setCurrentItem(mCurrentItem);
                    break;
                default:
                    break;
            }
        }
    }

    // 指引页面更改事件监听器
    class GuidePageChangeListener implements ViewPager.OnPageChangeListener {
        private int oldPosition = 0;

        @Override
        public void onPageScrollStateChanged(int position) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {
            mCurrentItem = position;
            oldPosition = position;
            Toast.makeText(BannerRecyclerViewActivity.this, "mCurrentItem=" + mCurrentItem, 0).show();
        }
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
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}