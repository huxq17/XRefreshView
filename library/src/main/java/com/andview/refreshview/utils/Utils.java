package com.andview.refreshview.utils;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
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
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
    }

    public static boolean isRecyclerViewFullscreen(RecyclerView viewGroup) {
        if (viewGroup.getAdapter() instanceof BaseRecyclerAdapter) {
            int count = viewGroup.getChildCount();

            View lastchild = viewGroup.getChildAt(count - 1);
            if (lastchild instanceof IFooterCallBack) {
                lastchild = viewGroup.getChildAt(count - 2);
            }
            if (lastchild == null) {
                return false;
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

            boolean atTop = findFirstCompletelyVisibleItemPosition(viewGroup) > 0;
            return (position[1] + lastchild.getHeight() + lastBottomMargin + bottomMargin + padding) >= height || atTop;
        }
        return false;
    }

    public static int findFirstCompletelyVisibleItemPosition(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int firstPosition;
        if (layoutManager instanceof LinearLayoutManager) {
            firstPosition = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof GridLayoutManager) {
            firstPosition = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
            staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(lastPositions);
            firstPosition = findMin(lastPositions);
        } else {
            throw new RuntimeException(
                    "Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
        }
        return firstPosition;
    }

    private static int findMin(int[] lastPositions) {
        int min = Integer.MAX_VALUE;
        for (int value : lastPositions) {
            if (value != RecyclerView.NO_POSITION && value < min)
                min = value;
        }
        return min;
    }

    public static int computeScrollDuration(int dx, int dy, int height) {
        int vx = 0;
        int vy = 0;
        final int absDx = Math.abs(dx);
        final int absDy = Math.abs(dy);
        final boolean horizontal = absDx > absDy;
        final int velocity = (int) Math.sqrt(vx * vx + vy * vy);
        final int delta = (int) Math.sqrt(dx * dx + dy * dy);
        final int containerSize = height;
        final int halfContainerSize = containerSize / 2;
        final float distanceRatio = Math.min(1.f, 1.f * delta / containerSize);
        final float distance = halfContainerSize + halfContainerSize *
                distanceInfluenceForSnapDuration(distanceRatio);

        final int duration;
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            float absDelta = (float) (horizontal ? absDx : absDy);
            duration = (int) (((absDelta / containerSize) + 1) * 300);
        }
        return Math.min(duration, 2000);
    }

    public static int computeScrollVerticalDuration(int dy, int height) {
        final int duration;
        float absDelta = (float) Math.abs(dy);
        duration = (int) (((absDelta / height) + 1) * 200);
        return Math.min(duration, 500);
    }

    private static float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }
}
