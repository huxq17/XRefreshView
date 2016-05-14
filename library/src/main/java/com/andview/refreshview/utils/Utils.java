package com.andview.refreshview.utils;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.andview.refreshview.callback.IFooterCallBack;
import com.andview.refreshview.recyclerview.BaseRecyclerAdapter;

public class Utils {

    /**
     * 格式化字符串
     *
     * @param format
     * @param args
     */
    public static String format(String format, int args) {
        return String.format(format, args);
    }

    public static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point(wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight());
        return point;
    }

    public static void setFullSpan(StaggeredGridLayoutManager.LayoutParams layoutParams) {
        if (layoutParams != null && !layoutParams.isFullSpan()) {
            layoutParams.setFullSpan(true);
        }
    }

    public static void removeViewFromParent(View view) {
        if (view == null) {
            return;
        }
        ViewParent parent = view.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(view);
        }
    }

    public static boolean isRecyclerViewFullscreen(RecyclerView viewGroup) {
        if (viewGroup.getAdapter() instanceof BaseRecyclerAdapter) {
            int count = viewGroup.getChildCount();

            View lastchild = viewGroup.getChildAt(count - 1);
            if (lastchild instanceof IFooterCallBack) {
                lastchild = viewGroup.getChildAt(count - 2);
            }
            RecyclerView.LayoutParams lastLp = (RecyclerView.LayoutParams) lastchild.getLayoutParams();
            int lastBottomMargin = lastLp.bottomMargin;
            WindowManager wm = (WindowManager) viewGroup.getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            int[] position = new int[2];
            int height = wm.getDefaultDisplay().getHeight();
            lastchild.getLocationOnScreen(position);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewGroup.getLayoutParams();
            int bottomMargin = layoutParams.bottomMargin;
            int padding = viewGroup.getPaddingBottom();
            return (position[1] + lastchild.getHeight() + lastBottomMargin + bottomMargin + padding) >= height;
        }
        return false;
    }

}
