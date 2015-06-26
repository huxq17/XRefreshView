package com.andview.refreshview.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class Utils {
	/**
	 * 
	 * @param child
	 * @param addView
	 * @param startChildY
	 * @param endChildY
	 * @param startAddY
	 * @param endAddY
	 * @param during
	 * @param listener
	 */
	public static void moveChildAndAddedView(View child, View addView,
			float startChildY, float endChildY, float startAddY, float endAddY,
			int during, AnimatorListener... listener) {
		LogUtils.i("startChildY=" + startChildY + ";endChildY=" + endChildY
				+ ";startAddY=" + startAddY + ";endAddY=" + endAddY);
		if (startAddY == endAddY || startChildY == endChildY) {
			return;
		}
		// 属性动画移动
		ObjectAnimator y = ObjectAnimator.ofFloat(child, "y", startChildY,
				endChildY);
		ObjectAnimator y2 = ObjectAnimator.ofFloat(addView, "y", startAddY,
				endAddY);

		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(y, y2);
		animatorSet.setDuration(during);
		if (listener.length > 0)
			animatorSet.addListener(listener[0]);
		animatorSet.start();
	}

	/**
	 * 格式化字符串
	 * 
	 * @param format
	 * @param args
	 */
	public static String format(String format, int args) {
		return String.format(format, args);
	}

	/**
	 * 兼容了Android 2.3.3
	 * 
	 * @param view
	 * @return
	 */
	@SuppressLint("NewApi")
	public static float getY(View view) {
		float y = 0;
		// view.getY()在api10之后才有
		if (Build.VERSION.SDK_INT <= 10) {
			final int[] location = new int[2];
			view.getLocationOnScreen(location);
			y = location[1];
		} else {
			y = view.getY();
			final int[] location = new int[2];
			view.getLocationOnScreen(location);
			// LogUtils.i("getY="+y+";getlocationOnScreen="+location[1]);
		}
		return y;
	}
}
