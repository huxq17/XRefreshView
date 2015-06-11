package com.example.xrefreshviewdemo.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.andview.refreshview.listener.OnBottomLoadMoreTime;
import com.example.xrefreshviewdemo.R;
import com.example.xrefreshviewdemo.recylerview.Person;
import com.example.xrefreshviewdemo.recylerview.PersonAdapter;

public class RecylerViewActivity extends Activity implements
		PersonAdapter.OnBottomListener {
	RecyclerView recyclerView;
	PersonAdapter adapter;
	List<Person> personList = new ArrayList<Person>();
	XRefreshView xRefreshView;
	int lastVisibleItem = 0;
	RecyclerView.LayoutManager layoutManager;
	private boolean isBottom = false;

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
		adapter = new PersonAdapter(personList);
		// adapter.setOnRecyclerViewListener(this);
		adapter.setOnBottomListener(this);
		recyclerView.setAdapter(adapter);

		xRefreshView.setXRefreshViewListener(new XRefreshViewListener() {

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

					@Override
					public void run() {
						xRefreshView.stopLoadMore();
					}
				}, 2000);
			}
		});
		// 现阶段XRefreshView对于上拉加载时机的判断仅支持api14也就是安卓4.0 以上的版本，
		// 如果想要兼容4.0以下，得自己设置上拉加载的时机,就像下面这样
		xRefreshView.setOnBottomLoadMoreTime(new OnBottomLoadMoreTime() {

			@Override
			public boolean isBottom() {
				return isBottom;
			}
		});
	}

	private void initData() {
		for (int i = 0; i < 20; i++) {
			Person person = new Person("name" + i, "" + i);
			personList.add(person);
		}
	}

	@Override
	public void isOnBottom(boolean isBottom) {
		this.isBottom = isBottom;
	}

}
