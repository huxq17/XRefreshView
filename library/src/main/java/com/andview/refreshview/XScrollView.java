package com.andview.refreshview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

public class XScrollView extends ScrollView {

    private OnScrollListener onScrollListener, mScrollListener;
    // 是否在触摸状态
    private boolean inTouch = false;
    // 上次滑动的最后位置
    private int lastT = 0;
    private XRefreshView mParent;
    private int mTouchSlop;
    private float lastY;

    public XScrollView(Context context) {
        super(context, null);
    }

    public XScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollListener == null) {
            return;
        }
        if (inTouch) {
            if (t != oldt) {
                // 有手指触摸，并且位置有滚动
                onScrollListener.onScrollStateChanged(this, OnScrollListener.SCROLL_STATE_TOUCH_SCROLL, isBottom());
                if (mScrollListener != null) {
                    mScrollListener.onScrollStateChanged(this, OnScrollListener.SCROLL_STATE_TOUCH_SCROLL, isBottom());
                }
            }
        } else {
            if (t != oldt) {
                // 没有手指触摸，并且位置有滚动，就可以简单的认为是在fling
                onScrollListener.onScrollStateChanged(this, OnScrollListener.SCROLL_STATE_FLING, isBottom());
                if (mScrollListener != null) {
                    mScrollListener.onScrollStateChanged(this, OnScrollListener.SCROLL_STATE_FLING, isBottom());
                }
                // 记住上次滑动的最后位置
                lastT = t;
                removeCallbacks(mRunnable);
                postDelayed(mRunnable, 20);
            }
        }
        onScrollListener.onScroll(l, t, oldl, oldt);
        if (mScrollListener != null) {
            mScrollListener.onScroll(l, t, oldl, oldt);
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (lastT == getScrollY() && !inTouch) {
                // 如果上次的位置和当前的位置相同，可认为是在空闲状态
                onScrollListener.onScrollStateChanged(XScrollView.this, OnScrollListener.SCROLL_STATE_IDLE, isBottom());
                if (mScrollListener != null) {
                    mScrollListener.onScrollStateChanged(XScrollView.this, OnScrollListener.SCROLL_STATE_IDLE, isBottom());
                }
            }
        }
    };

    private boolean isBottom() {
        return getScrollY() + getHeight() >= computeVerticalScrollRange();
    }

    protected void setOnScrollListener(XRefreshView parent, OnScrollListener scrollListener) {
        mParent = parent;
        this.onScrollListener = scrollListener;
        mParent.addTouchLifeCycle(new XRefreshView.TouchLifeCycle() {
            @Override
            public void onTouch(MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        lastY = event.getRawY();
                    case MotionEvent.ACTION_MOVE:
                        inTouch = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        inTouch = false;
                        lastT = getScrollY();
                        float curY = event.getRawY();
                        if (lastY - curY >= mTouchSlop) {
                            removeCallbacks(mRunnable);
                            postDelayed(mRunnable, 20);
                        }
                        break;
                }
            }
        });
    }

    /**
     * 设置XScrollView的滚动监听
     *
     * @param scrollListener
     */
    public void setOnScrollListener(OnScrollListener scrollListener) {
        mScrollListener = scrollListener;
    }

    /**
     * 滚动监听事件
     */
    public interface OnScrollListener {
        /**
         * The view is not scrolling. Note navigating the list using the
         * trackball counts as being in the idle state since these transitions
         * are not animated.
         */
        int SCROLL_STATE_IDLE = 0;

        /**
         * The user is scrolling using touch, and their finger is still on the
         * screen
         */
        int SCROLL_STATE_TOUCH_SCROLL = 1;

        /**
         * The user had previously been scrolling using touch and had performed
         * a fling. The animation is now coasting to a stop
         */
        int SCROLL_STATE_FLING = 2;

        /**
         * 滑动状态回调
         *
         * @param view         当前的scrollView
         * @param scrollState  当前的状态
         * @param arriveBottom 是否到达底部
         */
        void onScrollStateChanged(ScrollView view, int scrollState, boolean arriveBottom);

        /**
         * 滑动位置回调
         *
         * @param l
         * @param t
         * @param oldl
         * @param oldt
         */
        void onScroll(int l, int t, int oldl, int oldt);
    }
}
