package com.andview.example.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andview.example.R;
import com.andview.example.ui.BannerViewPager;
import com.andview.example.ui.CustomFooterView;
import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.SimpleXRefreshListener;
import com.andview.refreshview.recyclerview.BaseRecyclerAdapter;
import com.andview.refreshview.recyclerview.XSpanSizeLookup;

import java.util.ArrayList;
import java.util.List;

public class BannerRecyclerViewActivity extends Activity {
    private XRefreshView xRefreshView;
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private List<String> dataList = new ArrayList<>();
    private BannerViewPager bannerViewPager;
    private int[] bannerImages = new int[]{R.mipmap.test01, R.mipmap.test02, R.mipmap.test03};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_recyclerview);

        // 初始化数据
        for (int i = 0; i < 20; i++) {
            dataList.add("列表项 " + (i + 1));
        }

        // 初始化视图
        xRefreshView = findViewById(R.id.xrefreshview);
        recyclerView = findViewById(R.id.recyclerview);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(dataList);
        recyclerView.setAdapter(adapter);

        // 创建并设置banner header
        View headerView = LayoutInflater.from(this).inflate(R.layout.header_banner, null);
        bannerViewPager = headerView.findViewById(R.id.banner_viewpager);
        bannerViewPager.setAdapter(new BannerAdapter(bannerImages));
        adapter.setHeaderView(headerView, recyclerView);

        // 设置XRefreshView
        xRefreshView.setPullLoadEnable(true);
        xRefreshView.setCustomFooterView(new CustomFooterView(this));
        xRefreshView.setXRefreshViewListener(new SimpleXRefreshListener() {
            @Override
            public void onRefresh(boolean isPullDown) {
                // 模拟刷新数据
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dataList.clear();
                        for (int i = 0; i < 20; i++) {
                            dataList.add("刷新后的列表项 " + (i + 1));
                        }
                        adapter.notifyDataSetChanged();
                        xRefreshView.stopRefresh();
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore(boolean isSilence) {
                // 模拟加载更多数据
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int start = dataList.size();
                        for (int i = start; i < start + 10; i++) {
                            dataList.add("加载更多的列表项 " + (i + 1));
                        }
                        adapter.notifyDataSetChanged();
                        xRefreshView.stopLoadMore();
                    }
                }, 2000);
            }
        });
    }

    // 自定义适配器
    private class MyAdapter extends BaseRecyclerAdapter<MyAdapter.ViewHolder> {
        private List<String> mData;

        public MyAdapter(List<String> data) {
            mData = data;
        }

        @Override
        public ViewHolder getViewHolder(View view) {
            return new ViewHolder(view);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType, boolean isItem) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position, boolean isItem) {
            if (isItem) {
                holder.textView.setText(mData.get(position));
            }
        }

        @Override
        public int getAdapterItemCount() {
            return mData.size();
        }

        // 自定义ViewHolder
        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.text_view);
            }
        }
    }

    // Banner适配器
    private class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
        private int[] images;

        public BannerAdapter(int[] images) {
            this.images = images;
        }

        @Override
        public BannerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
            return new BannerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BannerViewHolder holder, int position) {
            holder.imageView.setImageResource(images[position % images.length]);
        }

        @Override
        public int getItemCount() {
            // 设置为 Integer.MAX_VALUE 实现无限循环
            return Integer.MAX_VALUE;
        }

        // Banner ViewHolder
        public class BannerViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;

            public BannerViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.banner_image);
            }
        }
    }
}