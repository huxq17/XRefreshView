package com.andview.refreshview.recyclerview;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.andview.refreshview.callback.IFooterCallBack;

import java.util.Collections;
import java.util.List;

/**
 * An abstract adapter which can be extended for Recyclerview
 */
public abstract class BaseRecyclerAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    protected View customLoadMoreView = null;
    protected View customHeaderView = null;
    private int id;

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPES.FOOTER) {
            VH viewHolder = getViewHolder(customLoadMoreView);
            if (getAdapterItemCount() == 0)
                viewHolder.itemView.setVisibility(View.GONE);
            return viewHolder;
        } else if (viewType == VIEW_TYPES.CHANGED_FOOTER) {
            VH viewHolder = getViewHolder(customLoadMoreView);
            if (getAdapterItemCount() == 0)
                viewHolder.itemView.setVisibility(View.GONE);
            return viewHolder;
        } else if (viewType == VIEW_TYPES.HEADER) {
            VH viewHolder = getViewHolder(customHeaderView);
            return viewHolder;
        }
        return onCreateViewHolder(parent);
    }

    public abstract VH getViewHolder(View view);

    public abstract VH onCreateViewHolder(ViewGroup parent);

    /**
     * 替代onBindViewHolder方法，实现这个方法就行了
     *
     * @param holder
     * @param position
     */
    public abstract void onBindViewHolder(VH holder, int position, boolean isItem);

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        int start = getStart();
        if (isHeader(position) || isFooter(position)) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) layoutParams).setFullSpan(true);
            }
        } else {
            onBindViewHolder(holder, position - start, true);
        }
    }

    /**
     * Using a custom LoadMoreView
     *
     * @param footerView the inflated view
     */
    public void setCustomLoadMoreView(View footerView) {
        if (footerView instanceof IFooterCallBack) {
            customLoadMoreView = footerView;
        } else {
            throw new RuntimeException("footerView must be implementes IFooterCallBack!");
        }
        notifyDataSetChanged();
    }

    public void setHeaderView(View headerView, RecyclerView recyclerView) {
        if (recyclerView == null) return;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager != null && layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new XSpanSizeLookup(this, gridLayoutManager.getSpanCount()));
        }
        customHeaderView = headerView;
        notifyDataSetChanged();
    }

    public View setHeaderView(@LayoutRes int id, RecyclerView recyclerView) {
        this.id = id;
        if (recyclerView == null) return null;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager != null && layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new XSpanSizeLookup(this, gridLayoutManager.getSpanCount()));
        }
        Context context = recyclerView.getContext();
        FrameLayout rootview = new FrameLayout(recyclerView.getContext());
        customHeaderView = LayoutInflater.from(context).inflate(id, rootview);
        notifyDataSetChanged();
        return customHeaderView;
    }

    public boolean isFooter(int position) {
        int start = getStart();
        return customLoadMoreView != null && position >= getAdapterItemCount() + start;
    }

    public boolean isHeader(int position) {
        return getStart() > 0 && position == 0;
    }

    /**
     * Changing the loadmore view
     *
     * @param customview the inflated view
     */
    public void swipeCustomLoadMoreView(View customview) {
        customLoadMoreView = customview;
        isLoadMoreChanged = true;
    }

    public View getCustomLoadMoreView() {
        return customLoadMoreView;
    }

    public boolean isLoadMoreChanged = false;

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return VIEW_TYPES.HEADER;
        } else if (isFooter(position)) {
            if (isLoadMoreChanged) {
                return VIEW_TYPES.CHANGED_FOOTER;
            } else {
                return VIEW_TYPES.FOOTER;
            }
        } else {
            return VIEW_TYPES.NORMAL;
        }
    }

    public int getStart() {
        return customHeaderView == null ? 0 : 1;
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        int count = getAdapterItemCount();
        count += getStart();
        if (customLoadMoreView != null) {
            count++;
        }
        return count;
    }

    public View getFooterView() {
        return customLoadMoreView;
    }

    /**
     * Returns the number of items in the adapter bound to the parent
     * RecyclerView.
     *
     * @return The number of items in the bound adapter
     */
    public abstract int getAdapterItemCount();

    /**
     * Swap the item of list
     *
     * @param list data list
     * @param from position from
     * @param to   position to
     */
    public void swapPositions(List<?> list, int from, int to) {
        Collections.swap(list, from, to);
    }

    /**
     * Insert a item to the list of the adapter
     *
     * @param list     data list
     * @param object   object T
     * @param position position
     * @param <T>      in T
     */
    public <T> void insert(List<T> list, T object, int position) {
        list.add(position, object);
        notifyItemInserted(position + getStart());
    }

    /**
     * Remove a item of the list of the adapter
     *
     * @param list     data list
     * @param position position
     */
    public void remove(List<?> list, int position) {
        if (list.size() > 0) {
            notifyItemRemoved(position + getStart());
        }
    }

    /**
     * Clear the list of the adapter
     *
     * @param list data list
     */
    public void clear(List<?> list) {
        int start = getStart();
        int size = list.size() + start;
        list.clear();
        notifyItemRangeRemoved(start, size);
    }

    protected class VIEW_TYPES {
        public static final int NORMAL = 0;
        public static final int FOOTER = 2;
        public static final int CHANGED_FOOTER = 3;
        public static final int HEADER = 4;
    }

    protected enum AdapterAnimationType {
        AlphaIn, SlideInBottom, ScaleIn, SlideInLeft, SlideInRight,
    }

    protected OnStartDragListener mDragStartListener = null;

    /**
     * Listener for manual initiation of a drag.
     */
    public interface OnStartDragListener {

        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
}
