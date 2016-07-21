package com.andview.example.ui.raindrop;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.callback.IFooterCallBack;
import com.andview.refreshview.utils.Utils;

public class CustomerFooter extends LinearLayout implements IFooterCallBack {
    private Context mContext;

    private View mContentView;
    private View mProgressBar;
    private TextView mHintView;
    private TextView mClickView;
    private boolean showing = false;

    public CustomerFooter(Context context) {
        super(context);
        initView(context);
    }

    public CustomerFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private RecyclerView recyclerView;

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public void callWhenNotAutoLoadMore(final XRefreshView xRefreshView) {
        if (recyclerView != null) {
            //当数据不满一屏时不显示点击加载更多
            show(Utils.isRecyclerViewFullscreen(recyclerView));
        }
        mClickView.setText(com.andview.refreshview.R.string.xrefreshview_footer_hint_click);
        mClickView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                xRefreshView.notifyLoadMore();
            }
        });
    }

    @Override
    public void onStateReady() {
        mHintView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mClickView.setVisibility(View.VISIBLE);
        mClickView.setText(com.andview.refreshview.R.string.xrefreshview_footer_hint_click);
    }

    @Override
    public void onStateRefreshing() {
        mHintView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mClickView.setVisibility(View.GONE);
        show(true);
    }

    @Override
    public void onReleaseToLoadMore() {
        mHintView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mClickView.setVisibility(View.VISIBLE);
        mClickView.setText(com.andview.refreshview.R.string.xrefreshview_footer_hint_release);
    }

    @Override
    public void onStateFinish(boolean hideFooter) {
        if (hideFooter) {
            mHintView.setText(com.andview.refreshview.R.string.xrefreshview_footer_hint_normal);
        } else {
            //处理数据加载失败时ui显示的逻辑，也可以不处理，看自己的需求
            mHintView.setText(com.andview.refreshview.R.string.xrefreshview_footer_hint_fail);
        }
        mHintView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mClickView.setVisibility(View.GONE);
    }

    @Override
    public void onStateComplete() {
        mHintView.setText(com.andview.refreshview.R.string.xrefreshview_footer_hint_complete);
        mHintView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        show(true);
    }

    @Override
    public void show(boolean show) {
        showing = show;
        LayoutParams lp = (LayoutParams) mContentView
                .getLayoutParams();
        lp.height = show ? LayoutParams.WRAP_CONTENT : 0;
        mContentView.setLayoutParams(lp);
    }

    @Override
    public boolean isShowing() {
        return showing;
    }

    private void initView(Context context) {
        mContext = context;
        ViewGroup moreView = (ViewGroup) LayoutInflater.from(mContext).inflate(com.andview.refreshview.R.layout.xrefreshview_footer, this);
        moreView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mContentView = moreView.findViewById(com.andview.refreshview.R.id.xrefreshview_footer_content);
        mProgressBar = moreView
                .findViewById(com.andview.refreshview.R.id.xrefreshview_footer_progressbar);
        mHintView = (TextView) moreView
                .findViewById(com.andview.refreshview.R.id.xrefreshview_footer_hint_textview);
        mClickView = (TextView) moreView
                .findViewById(com.andview.refreshview.R.id.xrefreshview_footer_click_textview);
    }

    @Override
    public int getFooterHeight() {
        return getMeasuredHeight();
    }
}
