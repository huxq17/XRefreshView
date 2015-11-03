package com.andview.refreshview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class XScrollView extends ScrollView {
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

	public XScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		View view = this.getChildAt(0);
		if (this.getHeight() + this.getScrollY() == view.getHeight()) {
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
