package com.example.xrefreshviewdemo.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.Menu;
import android.view.MenuItem;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshViewFooter;
import com.andview.refreshview.XRefreshView.SimpleXRefreshListener;
import com.example.xrefreshviewdemo.R;
import com.example.xrefreshviewdemo.recylerview.Person;
import com.example.xrefreshviewdemo.recylerview.SimpleAdapter;

public class RecylerViewActivity extends Activity {
	RecyclerView recyclerView;
	SimpleAdapter adapter;
	List<Person> personList = new ArrayList<Person>();
	XRefreshView xRefreshView;
	int lastVisibleItem = 0;
	LinearLayoutManager layoutManager;
	private boolean isBottom = false;
	private int mLoadCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recylerview);
		xRefreshView = (XRefreshView) findViewById(R.id.xrefreshview);
		xRefreshView.setPullLoadEnable(true);
		recyclerView = (RecyclerView) findViewById(R.id.recycler_view_test_rv);
		recyclerView.setHasFixedSize(true);

		layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);

		initData();
		adapter = new SimpleAdapter(personList);
		adapter.setCustomLoadMoreView(new XRefreshViewFooter(this));
		recyclerView.setAdapter(adapter);
		xRefreshView.setAutoLoadMore(false);
		xRefreshView.setPinnedTime(1000);
		xRefreshView.setMoveForHorizontal(true);
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
			public void onLoadMore() {
				new Handler().postDelayed(new Runnable() {
					public void run() {
						if (mLoadCount >= 3) {
							xRefreshView.setLoadComplete(true);
						}
						adapter.insert(new Person("More ", "21"),
								adapter.getAdapterItemCount());
						adapter.insert(new Person("More ", "21"),
								adapter.getAdapterItemCount());
						adapter.insert(new Person("More ", "21"),
								adapter.getAdapterItemCount());
						mLoadCount++;
						//刷新完成必须调用此方法停止加载
						xRefreshView.stopLoadMore();
					}
				}, 1000);
			}
		});
		recyclerView.addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				lastVisibleItem = layoutManager.findLastVisibleItemPosition();
			}

			public void onScrollStateChanged(RecyclerView recyclerView,
					int newState) {
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					isBottom = adapter.getItemCount() - 1 == lastVisibleItem;
				}
			};
		});
	}

	private void initData() {
		for (int i = 0; i < 20; i++) {
			Person person = new Person("name" + i, "" + i);
			personList.add(person);
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
			break;
		case R.id.menu_refresh:
			xRefreshView.startRefresh();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}