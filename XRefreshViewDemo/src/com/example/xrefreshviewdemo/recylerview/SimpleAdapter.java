package com.example.xrefreshviewdemo.recylerview;

import java.util.List;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andview.refreshview.recyclerview.UltimateViewAdapter;
import com.example.xrefreshviewdemo.R;

public class SimpleAdapter extends
		UltimateViewAdapter<SimpleAdapter.SimpleAdapterViewHolder> {
	private List<Person> list;

	public SimpleAdapter(List<Person> list) {
		this.list = list;
	}

	@Override
	public void onBindViewHolder(final SimpleAdapterViewHolder holder,
			int position) {
		if(position < getAdapterItemCount()){
			Person person = list.get(position);
			holder.nameTv.setText(String.valueOf(person.getName()));
			holder.ageTv.setText(person.getAge());
		}
	}

	@Override
	public int getAdapterItemCount() {
		return list.size();
	}

	@Override
	public SimpleAdapterViewHolder getViewHolder(View view) {
		return new SimpleAdapterViewHolder(view, false);
	}

	@Override
	public SimpleAdapterViewHolder onCreateViewHolder(ViewGroup parent) {
		View v = LayoutInflater.from(parent.getContext()).inflate(
				R.layout.item_recylerview, parent, false);
		SimpleAdapterViewHolder vh = new SimpleAdapterViewHolder(v, true);
		return vh;
	}

	public void insert(Person person, int position) {
		insert(list, person, position);
	}

	public void remove(int position) {
		remove(list, position);
	}

	public void clear() {
		clear(list);
	}

	@Override
	public long generateHeaderId(int position) {
		String name = getItem(position).getName();
		if (name.length() > 0)
			return name.charAt(0);
		else
			return -1;
	}

	@Override
	public SimpleAdapterViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(
				R.layout.item_recylerview, viewGroup, false);
		return new SimpleAdapterViewHolder(view, true);
	}

	@Override
	public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder,
			int position) {
		// viewHolder.itemView.setBackgroundColor(Color.parseColor("#AA70DB93"));
		viewHolder.itemView.setBackgroundColor(Color.parseColor("#AAffffff"));
		// ImageView imageView = (ImageView)
		// viewHolder.itemView.findViewById(R.id.stick_img);
		//
		// SecureRandom imgGen = new SecureRandom();
		// switch (imgGen.nextInt(3)) {
		// case 0:
		// imageView.setImageResource(R.drawable.test_back1);
		// break;
		// case 1:
		// imageView.setImageResource(R.drawable.test_back2);
		// break;
		// case 2:
		// imageView.setImageResource(R.drawable.test_back);
		// break;
		// }

	}

	public class SimpleAdapterViewHolder extends RecyclerView.ViewHolder {

		public View rootView;
		public TextView nameTv;
		public TextView ageTv;
		public int position;

		public SimpleAdapterViewHolder(View itemView, boolean isItem) {
			super(itemView);
			if (isItem) {
				nameTv = (TextView) itemView
						.findViewById(R.id.recycler_view_test_item_person_name_tv);
				ageTv = (TextView) itemView
						.findViewById(R.id.recycler_view_test_item_person_age_tv);
				rootView = itemView
						.findViewById(R.id.recycler_view_test_item_person_view);
			}

		}
	}

	public Person getItem(int position) {
		if (position < list.size())
			return list.get(position);
		else
			return null;
	}

}