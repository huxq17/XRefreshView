package com.andview.refreshview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.andview.refreshview.XRefreshView.XRefreshViewListener;

public class MainActivity extends Activity {
	private ListView lv;
	private List<String> str_name = new ArrayList<String>();
	private XRefreshView outView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		for (int i = 0; i < 20; i++) {
			str_name.add("数据" + i);
		}
		lv = (ListView) findViewById(R.id.lv);
		outView = (XRefreshView) findViewById(R.id.custom_view);
		outView.setPullLoadEnable(true);
		outView.setRefreshViewType(RefreshViewType.ABSLISTVIEW);
		lv.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, str_name));
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
