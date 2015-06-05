package com.example.xrefreshviewdemo.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.andview.refreshview.XRefreshViewBase;
import com.example.xrefreshviewdemo.R;
import com.example.xrefreshviewdemo.StickyListBean;
import com.example.xrefreshviewdemo.StickylistAdapter;
import com.example.xrefreshviewdemo.stickyListHeaders.StickyListHeadersListView;

public class CustomViewActivity extends Activity {
	private StickyListHeadersListView stickyLv;
	private List<StickyListBean> list = new ArrayList<StickyListBean>();
	private XRefreshView outView;
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
		outView = (XRefreshView) findViewById(R.id.custom_view);
		outView.setPullLoadEnable(true);
		outView.setAutoRefresh(true);
		outView.setRefreshBase(new XRefreshViewBase() {

			@Override
			public boolean isTop() {
				return stickyLv.getFirstVisiblePosition() == 0;
			}

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
		outView.setXRefreshViewListener(new XRefreshViewListener() {

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
			public void onLoadMore() {

				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						outView.stopLoadMore();
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
