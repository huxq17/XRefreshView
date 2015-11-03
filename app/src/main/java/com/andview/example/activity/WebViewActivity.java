package com.andview.example.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.andview.example.R;
import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.SimpleXRefreshListener;

public class WebViewActivity extends Activity {
	private XRefreshView outView;
	public static long lastRefreshTime;
	private WebView mWebView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		mWebView = (WebView) findViewById(R.id.wv);
		mWebView.loadUrl("http://www.baidu.com");
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
			@Override
			public void onPageFinished(WebView view, String url) {
				outView.stopRefresh();
				lastRefreshTime = outView.getLastRefreshTime();
				super.onPageFinished(view, url);
			}
		});
		outView = (XRefreshView) findViewById(R.id.custom_view);
		outView.setPullLoadEnable(true);
		outView.setXRefreshViewListener(new SimpleXRefreshListener() {

			@Override
			public void onRefresh() {
				mWebView.loadUrl("http://www.baidu.com");
			}

			@Override
			public void onLoadMore(boolean isSlience) {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						outView.stopLoadMore();
					}
				}, 2000);
			}
		});
		outView.restoreLastRefreshTime(lastRefreshTime);
	}

}
