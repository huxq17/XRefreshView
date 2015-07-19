package com.example.xrefreshviewdemo;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

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
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public void finishUpdate(View container) {
	}

	@Override
	public int getCount() {
		return images.length;
	}

	@Override
	public Object instantiateItem(ViewGroup view, int position) {
		View imageLayout = mInflater.inflate(R.layout.home_ads_view, view,
				false);
		ImageView imageView = (ImageView) imageLayout
				.findViewById(R.id.ads_view);
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(mContext, "click", Toast.LENGTH_SHORT).show();
			}
		});
		imageView.setScaleType(ScaleType.FIT_XY);
		final int index = position;
		imageView.setBackgroundResource(images[position]);

		((ViewPager) view).addView(imageLayout, 0);

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
