package com.andview.example;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class IndexPageAdapter extends PagerAdapter {

    private int[] images;
    private Context mContext;
    private LayoutInflater mInflater;

    public IndexPageAdapter(Context context, int[] images) {
        this.mContext = context;
        this.images = images;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public void finishUpdate(View container) {
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View imageLayout = mInflater.inflate(R.layout.home_ads_view, view, false);
        ImageView imageView = (ImageView) imageLayout
                .findViewById(R.id.ads_view);
        imageView.setScaleType(ScaleType.FIT_XY);
        final int index = position % images.length;
        imageView.setBackgroundResource(images[index]);

        view.addView(imageLayout, 0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(View container) {
    }

}
