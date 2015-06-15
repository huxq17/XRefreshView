package com.example.xrefreshviewdemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.example.xrefreshviewdemo.R;

public class ScrollViewActivity extends Activity {
	private XRefreshView outView;
	private LinearLayout ll;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scrollview);
		outView = (XRefreshView) findViewById(R.id.custom_view);
		ll = (LinearLayout) findViewById(R.id.ll);
		outView.setPullLoadEnable(true);
		outView.setAutoRefresh(false);
		//XRefreshView下拉刷新时机有了更强大的判断方法，已经不需要再设置view的类型了
//		outView.setRefreshViewType(XRefreshViewType.ABSLISTVIEW);
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
		for (int i = 0; i < 50; i++) {
			TextView tv = new TextView(this);
			tv.setText("数据"+i);
			ll.addView(tv);
		}
	}
}
