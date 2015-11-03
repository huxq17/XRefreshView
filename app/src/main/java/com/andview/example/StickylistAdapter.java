package com.andview.example;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.andview.example.stickyListHeaders.StickyListHeadersAdapter;
import java.util.ArrayList;
import java.util.List;

public class StickylistAdapter extends BaseAdapter implements
		StickyListHeadersAdapter, SectionIndexer {
	private List<StickyListBean> list = new ArrayList<StickyListBean>();
	private ViewHolder mHolder;
	private LayoutInflater mInflater;

	public StickylistAdapter(Context context, List<StickyListBean> list) {
		this.list = list;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	public void setData(List<StickyListBean> alarmList) {
		this.list = alarmList;
	}

	@Override
	public StickyListBean getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		mHolder = null;
		if (convertView == null) {
			convertView = mInflater
					.inflate(R.layout.item_sticky, parent, false);
			mHolder = new ViewHolder();
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.item);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		StickyListBean info = getItem(position);
		mHolder.tvTime.setText(info.content);
		return convertView;
	}

	public static class ViewHolder {
		private TextView tvTime;
	}

	@Override
	public Object[] getSections() {
		return null;
	}

	@Override
	public int getPositionForSection(int sectionIndex) {
		return 0;
	}

	@Override
	public int getSectionForPosition(int position) {
		if (position < getCount()) {
			return getItem(position).section;
		}
		return 0;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder holder;

		if (convertView == null) {
			holder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.item_sticky_header,
					parent, false);
			holder.text = (TextView) convertView.findViewById(R.id.header);
			convertView.setTag(holder);
		} else {
			holder = (HeaderViewHolder) convertView.getTag();
		}

		String headerText = getItem(position).YM;
		holder.text.setText(headerText);

		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		if (position < getCount()) {
			return getItem(position).section;
		}
		return 0;
	}

	static class HeaderViewHolder {
		TextView text;
	}

}
