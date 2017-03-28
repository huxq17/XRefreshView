package com.andview.example.recylerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andview.example.DensityUtil;
import com.andview.example.R;

import java.util.List;

public class NormalRecyclerAdapter extends RecyclerView.Adapter<NormalRecyclerAdapter.SimpleAdapterViewHolder> {
    private List<Person> list;
    private int largeCardHeight, smallCardHeight;

    public NormalRecyclerAdapter(List<Person> list, Context context) {
        this.list = list;
        largeCardHeight = DensityUtil.dip2px(context, 150);
        smallCardHeight = DensityUtil.dip2px(context, 100);
    }

    @Override
    public void onBindViewHolder(SimpleAdapterViewHolder holder, int position) {
        Person person = list.get(position);
        holder.nameTv.setText(person.getName());
        holder.ageTv.setText(person.getAge());
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
            holder.rootView.getLayoutParams().height = position % 2 != 0 ? largeCardHeight : smallCardHeight;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setData(List<Person> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public SimpleAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_recylerview, parent, false);
        SimpleAdapterViewHolder vh = new SimpleAdapterViewHolder(v, true);
        return vh;
    }

    public void insert(Person person, int position) {
        list.add(position, person);
        notifyItemInserted(position);
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
                        .findViewById(R.id.card_view);
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