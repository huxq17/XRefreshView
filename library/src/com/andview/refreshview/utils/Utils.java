package com.andview.refreshview.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.view.View;

public class Utils {
	public static void moveChildAndAddedView(View child, View addView,
			float childY, float addY, int during, AnimatorListener... listener) {
		// 属性动画移动
		ObjectAnimator y = ObjectAnimator.ofFloat(child, "y", child.getY(),
				childY);
		ObjectAnimator y2 = ObjectAnimator.ofFloat(addView, "y",
				addView.getY(), addY);

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
}
