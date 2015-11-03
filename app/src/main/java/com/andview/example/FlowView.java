package com.andview.example;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class FlowView extends ViewGroup {

	public FlowView(Context context) {
		super(context);
	}

	public FlowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FlowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			measureChild(child, widthMeasureSpec, heightMeasureSpec);
		}
	}

	public static class LayoutParams extends ViewGroup.MarginLayoutParams {
		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
		}
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new FlowView.LayoutParams(getContext(), attrs);
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int mViewGroupWidth = getMeasuredWidth();
		int top = getPaddingTop();

		int mPainterPosX = l;
		int mPainterPosY = t + top;
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);
			int width = childView.getMeasuredWidth();
			int height = childView.getMeasuredHeight();

			FlowView.LayoutParams margins = (LayoutParams) childView
					.getLayoutParams();
			int topMargin = margins.topMargin;
			int bottomMargin = margins.bottomMargin;
			int leftMargin = margins.leftMargin;
			int rightMargin = margins.rightMargin;
			// 如果剩余的空间不够，则移到下一行开始位置
			if (mPainterPosX + width + leftMargin + rightMargin > mViewGroupWidth) {
				mPainterPosX = l;
				mPainterPosY += height + topMargin + bottomMargin;
			}
			// 执行ChildView的绘制
			childView.layout(mPainterPosX + leftMargin, mPainterPosY
					+ topMargin, mPainterPosX + width + rightMargin,
					mPainterPosY + height + bottomMargin);
			// 记录当前已经绘制到的横坐标位置
			mPainterPosX += width + leftMargin + rightMargin;
		}
	};
}
