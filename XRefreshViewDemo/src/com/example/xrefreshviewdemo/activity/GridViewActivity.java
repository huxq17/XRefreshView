package com.example.xrefreshviewdemo.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.SimpleXRefreshListener;
import com.andview.refreshview.utils.LogUtils;
import com.example.xrefreshviewdemo.R;

public class GridViewActivity extends Activity {
	private GridView gv;
	private List<String> str_name = new ArrayList<String>();
	private XRefreshView outView;
	private ArrayAdapter<String> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gridview);
		for (int i = 0; i < 50; i++) {
			str_name.add("数据" + i);
		}
		gv = (GridView) findViewById(R.id.gv);
		outView = (XRefreshView) findViewById(R.id.custom_view);
		outView.setPullLoadEnable(true);
		// XRefreshView下拉刷新时机有了更强大的判断方法，已经不需要再设置view的类型了
		// outView.setRefreshViewType(XRefreshViewType.ABSLISTVIEW);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, str_name);
		gv.setAdapter(adapter);
		outView.setXRefreshViewListener(new SimpleXRefreshListener() {
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
				final List<String> addlist = new ArrayList<String>();
				for (int i = 0; i < 20; i++) {
					addlist.add("数据" + (i + str_name.size()));
				}

				new Handler().postDelayed(new Runnable() {

					@SuppressLint("NewApi")
					@Override
					public void run() {
						if (Build.VERSION.SDK_INT >= 11) {
							str_name.addAll(addlist);
							adapter.addAll(addlist);
						}
						outView.stopLoadMore();
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
}
