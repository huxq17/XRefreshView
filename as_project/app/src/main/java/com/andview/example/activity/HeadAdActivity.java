package com.andview.example.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.andview.example.IndexPageAdapter;
import com.andview.example.R;
import com.andview.example.ui.AdHeader;
import com.andview.example.ui.LoopViewPager;
import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.SimpleXRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeadAdActivity extends Activity {
	private GridView gv;
	private List<String> str_name = new ArrayList<String>();
	private XRefreshView outView;
	private ArrayAdapter<String> adapter;
	private LoopViewPager mLoopViewPager;
	private AdHeader headerView;
	private int[] mImageIds = new int[] { R.mipmap.test01, R.mipmap.test02,
			R.mipmap.test03 };// 测试图片id
	private ScheduledExecutorService mScheduledExecutorService;// 启动广告自动切换服务

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gridview);
		for (int i = 0; i < 50; i++) {
			str_name.add("数据" + i);
		}
		gv = (GridView) findViewById(R.id.gv);
		outView = (XRefreshView) findViewById(R.id.custom_view);

		headerView = new AdHeader(this);
		mLoopViewPager = (LoopViewPager) headerView
				.findViewById(R.id.index_viewpager);
		initViewPager();
		
		outView.setPullLoadEnable(true);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, str_name);
		gv.setAdapter(adapter);
		outView.setPinnedTime(1000);
		// outView.setAutoLoadMore(false);
		outView.setCustomHeaderView(headerView);
		// outView.setCustomHeaderView(new XRefreshViewHeader(this));
		outView.setMoveForHorizontal(true);
		outView.setPinnedContent(true);
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
						if (str_name.size() <= 70) {
							if (Build.VERSION.SDK_INT >= 11) {
								str_name.addAll(addlist);
								adapter.addAll(addlist);
							}
							outView.stopLoadMore();
						} else {
							outView.setLoadComplete(true);
						}
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
			str_name.clear();
			for (int i = 0; i < 50; i++) {
				str_name.add("数据" + i);
			}
			adapter.notifyDataSetChanged();
			outView.setLoadComplete(false);
			break;
		case R.id.menu_refresh:
			outView.startRefresh();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initViewPager() {
		IndexPageAdapter pageAdapter = new IndexPageAdapter(this,
				mImageIds);
		// // 把图片等比例放缩
		// if (Constant.mScreenWidth != 0) {
		// FrameLayout.LayoutParams params =
		// (android.widget.FrameLayout.LayoutParams) mLoopViewPager
		// .getLayoutParams();
		// params.height = (int) (Constant.mScreenWidth * (380.0 / 640.0));
		// mLoopViewPager.setLayoutParams(params);
		// }

		if (mImageIds.length == 1) {
			mLoopViewPager.setPagingEnabled(true);// 如果是一张图片设置不能滑动
		}
		mLoopViewPager.setAdapter(pageAdapter);
		mLoopViewPager.setOnPageChangeListener(new GuidePageChangeListener());

		mLoopViewPager.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					// 开始图片滚动
					if (mScheduledExecutorService != null
							&& mScheduledExecutorService.isShutdown()) {
						mScheduledExecutorService = Executors
								.newSingleThreadScheduledExecutor();
						mScheduledExecutorService.scheduleAtFixedRate(
								new ScrollTask(), 1, 5, TimeUnit.SECONDS);

					}
					break;
				default:
					// 停止图片滚动
					mScheduledExecutorService.shutdown();
					break;
				}
				return false;

			}
		});
	}

	private int mCurrentItem = 0;
	private UIHandler mUiHandler;

	private class ScrollTask implements Runnable {

		public void run() {
			synchronized (mLoopViewPager) {
				mCurrentItem = (mCurrentItem + 1) % mImageIds.length;
				Message msg = mUiHandler.obtainMessage();
				msg.what = 1000;
				msg.sendToTarget(); // 通过Handler切换图片
			}
		}
	}

	private class UIHandler extends Handler {
		@Override
		public void dispatchMessage(Message msg) {
			super.dispatchMessage(msg);
			switch (msg.what) {
			case 1000:
				mLoopViewPager.setCurrentItem(mCurrentItem);
				break;
			default:
				break;
			}
		}
	}

	// 指引页面更改事件监听器
	class GuidePageChangeListener implements OnPageChangeListener {
		private int oldPosition = 0;

		@Override
		public void onPageScrollStateChanged(int position) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int position) {
			mCurrentItem = position;
			oldPosition = position;
		}
	}
}
