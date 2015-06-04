package com.andview.refreshview.example;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.andview.refreshview.R;
import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.andview.refreshview.XRefreshViewType;

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
		outView.setRefreshViewType(XRefreshViewType.ABSLISTVIEW);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, str_name);
		gv.setAdapter(adapter);
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
				final List<String> addlist = new ArrayList<String>();
				for (int i = 0; i < 20; i++) {
					addlist.add("数据" + (i+str_name.size()));
				}
				
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						str_name.addAll(addlist);
						adapter.addAll(addlist);
						outView.stopLoadMore();
					}
				}, 2000);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
