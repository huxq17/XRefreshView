package com.andview.refreshview.utils;

import android.view.View;
import android.widget.LinearLayout;

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

    public static void setBottomMargin(View view, int height) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view
                .getLayoutParams();
        lp.bottomMargin = height;
        view.setLayoutParams(lp);
    }

    public static void setTopMargin(View view, int height) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view
                .getLayoutParams();
        lp.topMargin = height;
        view.setLayoutParams(lp);
    }
}
