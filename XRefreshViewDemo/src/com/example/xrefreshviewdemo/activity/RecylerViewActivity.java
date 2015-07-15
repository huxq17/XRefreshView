package com.example.xrefreshviewdemo.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.LayoutInflater;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.SimpleXRefreshListener;
import com.example.xrefreshviewdemo.R;
import com.example.xrefreshviewdemo.recylerview.Person;
import com.example.xrefreshviewdemo.recylerview.SimpleAdapter;

public class RecylerViewActivity extends Activity{
	RecyclerView recyclerView;
	SimpleAdapter adapter;
	List<Person> personList = new ArrayList<Person>();
	XRefreshView xRefreshView;
	int lastVisibleItem = 0;
	LinearLayoutManager layoutManager;
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
		adapter = new SimpleAdapter(personList);
//		adapter.setCustomLoadMoreView(LayoutInflater.from(this)
//                .inflate(R.layout.custom_footer, null));
		recyclerView.setAdapter(adapter);

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
			public void onRecyclerViewLoadMore(int itemsCount,
					int maxLastVisiblePosition) {
				new Handler().postDelayed(new Runnable() {
                    public void run() {
                        adapter.insert(new Person("More ", "21"), adapter.getAdapterItemCount());
                        adapter.insert(new Person("More ", "21"), adapter.getAdapterItemCount());
                        adapter.insert(new Person("More ", "21"), adapter.getAdapterItemCount());
                        // linearLayoutManager.scrollToPositionWithOffset(maxLastVisiblePosition,-1);
                        //   linearLayoutManager.scrollToPosition(maxLastVisiblePosition);

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
		// 现阶段XRefreshView对于上拉加载时机的判断仅支持api14也就是安卓4.0 以上的版本，
		// 如果想要兼容4.0以下，得自己设置上拉加载的时机,就像下面这样
		// xRefreshView.setOnBottomLoadMoreTime(new OnBottomLoadMoreTime() {
		//
		// @Override
		// public boolean isBottom() {
		// return isBottom;
		// }
		// });
	}

	private void initData() {
		for (int i = 0; i < 20; i++) {
			Person person = new Person("name" + i, "" + i);
			personList.add(person);
		}
	}

}