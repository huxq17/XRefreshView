package com.andview.example.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.andview.example.R;
import com.andview.example.recylerview.Person;
import com.andview.example.recylerview.SimpleAdapter;
import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.SimpleXRefreshListener;
import com.andview.refreshview.XRefreshViewFooter;
import com.andview.refreshview.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class NotFullScreenActivity extends Activity {
    RecyclerView recyclerView;
    SimpleAdapter adapter;
    List<Person> personList = new ArrayList<Person>();
    XRefreshView xRefreshView;
    int lastVisibleItem = 0;
    //    GridLayoutManager layoutManager;
    LinearLayoutManager layoutManager;
    private boolean isBottom = false;
    private int mLoadCount = 0;
    private boolean isList = true;//false 为grid布局

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recylerview2);
        xRefreshView = (XRefreshView) findViewById(R.id.xrefreshview);
        xRefreshView.setPullLoadEnable(true);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_test_rv);
        recyclerView.setHasFixedSize(true);

        adapter = new SimpleAdapter(personList, this);
        // 设置静默加载模式
//        xRefreshView1.setSilenceLoadMore();
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // 静默加载模式不能设置footerview
        recyclerView.setAdapter(adapter);
        //设置刷新完成以后，headerview固定的时间
        xRefreshView.setPinnedTime(1000);
        xRefreshView.setPullLoadEnable(true);
        xRefreshView.setMoveForHorizontal(true);
        xRefreshView.setAutoLoadMore(true);
        xRefreshView.setEmptyView(R.layout.layout_emptyview);
        showXRefreshView(false);

        adapter.setCustomLoadMoreView(new XRefreshViewFooter(this));
        //设置静默加载时提前加载的item个数
//        xRefreshView1.setPreLoadCount(4);

        xRefreshView.setXRefreshViewListener(new SimpleXRefreshListener() {

            @Override
            public void onRefresh(boolean isPullDown) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        xRefreshView.stopRefresh();
                    }
                }, 500);
            }

            @Override
            public void onLoadMore(boolean isSilence) {
                LogUtils.e("loadmore");
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        for (int i = 0; i < 1; i++) {
                            adapter.insert(new Person("More ", mLoadCount + "21"),
                                    adapter.getAdapterItemCount());
                        }
                        LogUtils.i("test onLoadMore recyclerviewAdapter.count=" + adapter.getItemCount());
                        mLoadCount++;

                        if (mLoadCount >= 5) {
                            xRefreshView.setLoadComplete(true);
                        } else {
                            // 刷新完成必须调用此方法停止加载
                            xRefreshView.stopLoadMore();
                        }
                    }
                }, 1000);
            }
        });
        //如果想在数据加载完成以后不隐藏footerview则需要调用下面这行代码并传入false
//        xRefreshView1.setHideFooterWhenComplete(false);
        requestData();
//		// 实现Recyclerview的滚动监听
//		xRefreshView1.setOnRecyclerViewScrollListener(new OnScrollListener() {
//			@Override
//			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//				super.onScrolled(recyclerView, dx, dy);
//				lastVisibleItem = layoutManager.findLastVisibleItemPosition();
//			}
//
//			public void onScrollStateChanged(RecyclerView recyclerView,
//											 int newState) {
//				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//					isBottom = recyclerviewAdapter.getItemCount() - 1 == lastVisibleItem;
//				}
//			}
//		});
    }

    public void showXRefreshView(boolean show) {
        xRefreshView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void requestData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initData();
                adapter.setData(personList);
            }
        }, 1000);
    }

    private void initData() {
        for (int i = 0; i < 1; i++) {
            Person person = new Person("name" + i, "" + i);
            personList.add(person);
            showXRefreshView(true);
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
                mLoadCount = 0;
                xRefreshView.setLoadComplete(false);
                //切换布局
                isList = !isList;

                if (isList) {
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(layoutManager);
                } else {
                    recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
                }
                //当切换layoutManager时，需调用此方法
                xRefreshView.notifyLayoutManagerChanged();
                break;
            case R.id.menu_refresh:
                xRefreshView.startRefresh();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}