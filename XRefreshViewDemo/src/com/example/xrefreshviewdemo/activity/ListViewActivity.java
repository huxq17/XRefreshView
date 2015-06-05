package com.example.xrefreshviewdemo.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.andview.refreshview.XRefreshViewType;
import com.example.xrefreshviewdemo.R;

public class ListViewActivity extends Activity {
	private ListView lv;
	private List<String> str_name = new ArrayList<String>();
	private XRefreshView outView;
	private ArrayAdapter<String> adapter;
	public static long lastRefreshTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listview);
		for (int i = 0; i < 10; i++) {
			str_name.add("数据" + i);
		}
		lv = (ListView) findViewById(R.id.lv);
		outView = (XRefreshView) findViewById(R.id.custom_view);
		outView.setPullLoadEnable(true);
		outView.setRefreshViewType(XRefreshViewType.ABSLISTVIEW);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, str_name);
		lv.setAdapter(adapter);
		outView.setXRefreshViewListener(new XRefreshViewListener() {

			@Override
			public void onRefresh() {
				
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						outView.stopRefresh();
						lastRefreshTime = outView.getLastRefreshTime();
					}
				}, 2000);
			}

			@Override
			public void onLoadMore() {
//				final List<String> addlist = new ArrayList<String>();
//				for (int i = 0; i < 20; i++) {
//					addlist.add("数据" + (i+str_name.size()));
//				}
				
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
//						str_name.addAll(addlist);
//						adapter.addAll(addlist);
						outView.stopLoadMore();
					}
				}, 2000);
			}
		});
		outView.restoreLastRefreshTime(lastRefreshTime);
		outView.setAutoRefresh(true);
	}

}
