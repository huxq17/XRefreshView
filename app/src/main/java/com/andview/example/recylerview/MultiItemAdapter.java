package com.andview.example.recylerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andview.example.R;
import com.andview.refreshview.recyclerview.BaseRecyclerAdapter;

import java.util.List;

public class MultiItemAdapter extends BaseRecyclerAdapter<MultiItemAdapter.SimpleAdapterViewHolder> {
    private List<Person> list;

    public MultiItemAdapter(List<Person> list) {
        this.list = list;
    }

    @Override
    public void onBindViewHolder(SimpleAdapterViewHolder holder, int position, boolean isItem) {
        Person person = list.get(position);
        int type = getAdapterItemViewType(position);
        if (type == 0) {
            holder.tvLeft.setText(person.getName());
        } else {
            holder.tvRight.setText(person.getName());
        }
    }

    @Override
    public int getAdapterItemViewType(int position) {
        if (list != null && list.size() > 0) {
            return list.get(position).getType();
        }
        return 0;
    }

    @Override
    public int getAdapterItemCount() {
        return list.size();
    }

    @Override
    public SimpleAdapterViewHolder getViewHolder(View view) {
        return new SimpleAdapterViewHolder(view, false);
    }

    public void setData(List<Person> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public SimpleAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType, boolean isItem) {
        View v = null;
        if (viewType == 0) {
            v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_left_recylerview, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_right_recylerview, parent, false);
        }
        return new SimpleAdapterViewHolder(v, viewType, true);
    }

    public class SimpleAdapterViewHolder extends RecyclerView.ViewHolder {

        public TextView tvRight, tvLeft;

        public SimpleAdapterViewHolder(View itemView, boolean isItem) {
            super(itemView);
            init(itemView, -1, isItem);
        }

        public SimpleAdapterViewHolder(View itemView, int viewType, boolean isItem) {
            super(itemView);
            init(itemView, viewType, isItem);
        }

        private void init(View itemView, int viewType, boolean isItem) {
            if (isItem) {
                switch (viewType) {
                    case 0:
                        tvLeft = (TextView) itemView.findViewById(R.id.tv_multi_left);
                        break;
                    default:
                        tvRight = (TextView) itemView.findViewById(R.id.tv_multi_right);
                        break;
                }
            }
        }
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

    public Person getItem(int position) {
        if (position < list.size())
            return list.get(position);
        else
            return null;
    }

}