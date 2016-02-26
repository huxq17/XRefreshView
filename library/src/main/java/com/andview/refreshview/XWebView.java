package com.andview.refreshview;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class XWebView extends WebView {
	private OnScrollBottomListener _listener;
	private int _calCount;

	public interface OnScrollBottomListener {
		void srollToBottom();
	}

	public void registerOnBottomListener(OnScrollBottomListener l) {
		_listener = l;
	}

	public void unRegisterOnBottomListener() {
		_listener = null;
	}

	public XWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		if (this.getHeight() + this.getScrollY() == getHeight()) {
			_calCount++;
			if (_calCount == 1) {
				if (_listener != null) {
					_listener.srollToBottom();
				}
			}
		} else {
			_calCount = 0;
		}
	}
}
