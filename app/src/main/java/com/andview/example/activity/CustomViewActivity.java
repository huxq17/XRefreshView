package com.andview.example.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.andview.example.R;
import com.andview.example.StickyListBean;
import com.andview.example.StickylistAdapter;
import com.andview.example.stickyListHeaders.StickyListHeadersListView;
import com.andview.example.ui.CustomHeader;
import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.listener.OnBottomLoadMoreTime;
import com.andview.refreshview.listener.OnTopRefreshTime;

import java.util.ArrayList;
import java.util.List;

public class CustomViewActivity extends Activity {
	private StickyListHeadersListView stickyLv;
	private List<StickyListBean> list = new ArrayList<StickyListBean>();
	private XRefreshView refreshView;
	private int mTotalItemCount;
	private StickylistAdapter adapter;


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
		refreshView.setAutoRefresh(true);
//		refreshView.setPinnedTime(0);
		refreshView.setCustomHeaderView(new CustomHeader(this));
		refreshView.setOnTopRefreshTime(new OnTopRefreshTime() {

			@Override
			public boolean isTop() {
				return stickyLv.getFirstVisiblePosition() == 0;
			}
		});
		refreshView.setOnBottomLoadMoreTime(new OnBottomLoadMoreTime() {

			@Override
			public boolean isBottom() {
				return stickyLv.getLastVisiblePosition() == mTotalItemCount - 1;
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
			public void onRefresh() {

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						refreshView.stopRefresh();
					}
				}, 10000);
			}

			@Override
			public void onLoadMore(boolean isSlience) {

				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						refreshView.stopLoadMore();
					}
				}, 2000);
			}
		});
	}

	int section = 1;
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
