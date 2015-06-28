package com.example.xrefreshviewdemo.recylerview;

import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andview.refreshview.utils.LogUtils;
import com.example.xrefreshviewdemo.R;

public class PersonAdapter extends RecyclerView.Adapter {
	public static interface OnRecyclerViewListener {
		void onItemClick(int position);

		boolean onItemLongClick(int position);
	}

	private OnRecyclerViewListener onRecyclerViewListener;

	public void setOnRecyclerViewListener(
			OnRecyclerViewListener onRecyclerViewListener) {
		this.onRecyclerViewListener = onRecyclerViewListener;
	}

	private static final String TAG = PersonAdapter.class.getSimpleName();
	private List<Person> list;

	public PersonAdapter(List<Person> list) {
		this.list = list;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(
				R.layout.item_recylerview, null);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		view.setLayoutParams(lp);
		return new PersonViewHolder(view);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder,
			int position) {
		PersonViewHolder holder = (PersonViewHolder) viewHolder;
		holder.position = position;
		Person person = list.get(position);
		holder.nameTv.setText(person.getName());
		holder.ageTv.setText(person.getAge() + "岁");
		LogUtils.i("position="+position+";size="+list.size());
//		if(bottomListener!=null){
//			if (position == list.size() - 1) {
//				bottomListener.isOnBottom(true);
//			} else {
//				bottomListener.isOnBottom(false);
//			}
//		}
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	class PersonViewHolder extends RecyclerView.ViewHolder implements
			View.OnClickListener, View.OnLongClickListener {
		public View rootView;
		public TextView nameTv;
		public TextView ageTv;
		public int position;

		public PersonViewHolder(View itemView) {
			super(itemView);
			nameTv = (TextView) itemView
					.findViewById(R.id.recycler_view_test_item_person_name_tv);
			ageTv = (TextView) itemView
					.findViewById(R.id.recycler_view_test_item_person_age_tv);
			rootView = itemView
					.findViewById(R.id.recycler_view_test_item_person_view);
			rootView.setOnClickListener(this);
			rootView.setOnLongClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if (null != onRecyclerViewListener) {
				onRecyclerViewListener.onItemClick(position);
			}
		}

		@Override
		public boolean onLongClick(View v) {
			if (null != onRecyclerViewListener) {
				return onRecyclerViewListener.onItemLongClick(position);
			}
			return false;
		}
	}

	private OnBottomListener bottomListener;

	public void setOnBottomListener(OnBottomListener bottomListener) {
		this.bottomListener = bottomListener;
	}

	public interface OnBottomListener {
		void isOnBottom(boolean isBottom);
	}
}
