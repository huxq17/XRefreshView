package com.andview.refreshview.view

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

/**
 * Created by 2144 on 2017/4/25.
 */
open class XWebView @JvmOverloads constructor(
       private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(mContext, attrs, defStyleAttr) {

    val isBottom: Boolean
        get() = computeVerticalScrollRange() == height + scrollY

    public override fun computeVerticalScrollRange(): Int {
        return super.computeVerticalScrollRange()
    }
}