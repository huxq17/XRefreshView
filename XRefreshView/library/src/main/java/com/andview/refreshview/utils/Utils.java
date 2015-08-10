package com.andview.refreshview.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

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
}
