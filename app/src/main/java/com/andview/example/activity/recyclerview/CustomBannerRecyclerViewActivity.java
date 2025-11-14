package com.andview.example.activity.recyclerview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.andview.example.R;
import com.andview.example.recylerview.Person;
import com.andview.example.recylerview.SimpleAdapter;
import com.andview.example.ui.BannerViewPager;
import com.andview.example.ui.CustomGifHeader;
import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.SimpleXRefreshListener;
import com.andview.refreshview.XRefreshViewFooter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomBannerRecyclerViewActivity extends Activity {
    RecyclerView recyclerView;
    SimpleAdapter adapter;
    List<Person> personList = new ArrayList<Person>();
    XRefreshView xRefreshView;
    int lastVisibleItem = 0;
    LinearLayoutManager layoutManager;
    private boolean isBottom = false;
    private int mLoadCount = 0;
    private View headerView;
    private BannerViewPager mBannerViewPager;
    private List<String> bannerImageUrls = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recylerview);
        xRefreshView = (XRefreshView) findViewById(R.id.xrefreshview);
        xRefreshView.setPullLoadEnable(true);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_test_rv);
        recyclerView.setHasFixedSize(true);

        initData();
        initBannerData();
        
        adapter = new SimpleAdapter(personList, this);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        
        // 添加header banner
        headerView = LayoutInflater.from(this).inflate(R.layout.custom_bannerview, null);
        mBannerViewPager = (BannerViewPager) headerView.findViewById(R.id.banner_viewpager);
        initBannerViewPager();
        adapter.setHeaderView(headerView, recyclerView);
        
        // 设置刷新头
        CustomGifHeader header = new CustomGifHeader(this);
        xRefreshView.setCustomHeaderView(header);
        
        // 设置加载更多脚
        adapter.setCustomLoadMoreView(new XRefreshViewFooter(this));
        
        recyclerView.setAdapter(adapter);
        
        // 设置刷新和加载更多监听
        xRefreshView.setXRefreshViewListener(new SimpleXRefreshListener() {
            @Override
            public void onRefresh(boolean isPullDown) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 模拟数据刷新
                        refreshData();
                        xRefreshView.stopRefresh();
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore(boolean isSilence) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        // 模拟加载更多数据
                        loadMoreData();
                        
                        mLoadCount++;
                        if (mLoadCount >= 3) {
                            xRefreshView.setLoadComplete(true);
                        } else {
                            xRefreshView.stopLoadMore(false);
                        }
                    }
                }, 1000);
            }
        });
    }

    private void initData() {
        for (int i = 0; i < 10; i++) {
            Person person = new Person("name" + i, "" + i);
            personList.add(person);
        }
    }

    private void initBannerData() {
        // 添加一些示例图片URL
        bannerImageUrls.add("https://via.placeholder.com/600x200?text=Banner+1");
        bannerImageUrls.add("https://via.placeholder.com/600x200?text=Banner+2");
        bannerImageUrls.add("https://via.placeholder.com/600x200?text=Banner+3");
        bannerImageUrls.add("https://via.placeholder.com/600x200?text=Banner+4");
    }

    private void initBannerViewPager() {
        mBannerViewPager.setAdapter(new BannerAdapter());
        mBannerViewPager.setParent(recyclerView);
    }

    private void refreshData() {
        // 清空现有数据
        personList.clear();
        
        // 添加新数据
        for (int i = 0; i < 10; i++) {
            Person person = new Person("Refresh name" + i, "" + i);
            personList.add(person);
        }
        
        // 通知适配器数据变化
        adapter.notifyDataSetChanged();
        
        // 重置加载更多状态
        mLoadCount = 0;
        xRefreshView.setLoadComplete(false);
    }

    private void loadMoreData() {
        // 添加更多数据
        int start = personList.size();
        for (int i = start; i < start + 6; i++) {
            Person person = new Person("Load More name" + i, "" + i);
            personList.add(person);
        }
        
        // 通知适配器数据变化
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch (menuId) {
            case R.id.menu_clear:
                mLoadCount = 0;
                xRefreshView.setLoadComplete(false);
                break;
            case R.id.menu_refresh:
                xRefreshView.startRefresh();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // 自定义Banner适配器
    private class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
        
        @Override
        public BannerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
            return new BannerViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(BannerViewHolder holder, int position) {
            String imageUrl = bannerImageUrls.get(position % bannerImageUrls.size());
            Picasso.get().load(imageUrl).into(holder.bannerImageView);
            holder.bannerTextView.setText("Banner " + (position % bannerImageUrls.size() + 1));
        }
        
        @Override
        public int getItemCount() {
            // 返回较大的数，实现无限循环
            return Integer.MAX_VALUE;
        }
        
        class BannerViewHolder extends RecyclerView.ViewHolder {
            ImageView bannerImageView;
            TextView bannerTextView;
            
            public BannerViewHolder(View itemView) {
                super(itemView);
                bannerImageView = (ImageView) itemView.findViewById(R.id.banner_image);
                bannerTextView = (TextView) itemView.findViewById(R.id.banner_text);
            }
        }
    }
}