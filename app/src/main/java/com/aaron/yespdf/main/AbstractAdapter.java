package com.aaron.yespdf.main;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.EmptyHolder;
import com.aaron.yespdf.common.widgets.BorderImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
abstract class AbstractAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_EMPTY = 1;

    protected Context context;
    protected LayoutInflater inflater;
    protected ICommInterface<T> commInterface;

    protected List<T> sourceList;
    protected List<T> selectList;

    protected boolean selectMode;
    protected SparseBooleanArray checkArray;

    AbstractAdapter(ICommInterface<T> commInterface, List<T> sourceList) {
        this.commInterface = commInterface;
        this.sourceList = sourceList;
        selectList = new ArrayList<>();
        checkArray = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (this.context == null) context = parent.getContext();
        if (inflater == null) inflater = LayoutInflater.from(context);
        if (viewType == TYPE_EMPTY) {
            View itemView = inflater.inflate(R.layout.app_recycler_item_emptyview, parent, false);
            return new EmptyHolder(itemView);
        }
        RecyclerView.ViewHolder holder = createHolder(parent, viewType);
        holder.itemView.setOnClickListener(v -> onTap(holder, holder.getAdapterPosition()));
        holder.itemView.setOnLongClickListener(v -> {
            commInterface.onStartOperation();
            selectMode = true;
            notifyItemRangeChanged(0, getItemCount(), 0);
            return true;
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        bindHolder(viewHolder, position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            bindHolder(viewHolder, position);
        } else {
            bindHolder(viewHolder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        if (sourceList.isEmpty()) {
            return 1;
        }
        return itemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (sourceList.isEmpty()) {
            return TYPE_EMPTY;
        }
        return super.getItemViewType(position);
    }

    void selectAll(boolean selectAll) {
        for (int i = 0; i < getItemCount(); i++) {
            checkArray.put(i, selectAll);
        }
        selectList.clear();
        if (selectAll) {
            for (int i = 0; i < getItemCount(); i++) {
                selectList.add(sourceList.get(i));
            }
        }
        commInterface.onSelect(selectList, selectAll);
        notifyItemRangeChanged(0, getItemCount(), 0);
    }

    void cancelSelect() {
        selectMode = false;
        checkArray.clear();
        selectList.clear();
        notifyItemRangeChanged(0, getItemCount(), 0);
    }

    @NonNull
    abstract RecyclerView.ViewHolder createHolder(@NonNull ViewGroup parent, int viewType);

    abstract void bindHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position);

    abstract void bindHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List<Object> payloads);

    abstract int itemCount();

    abstract void onTap(RecyclerView.ViewHolder viewHolder, int position);

    interface ICommInterface<T> {
        void onStartOperation();

        void onSelect(List<T> list, boolean selectAll);
    }

    static class CoverHolder extends RecyclerView.ViewHolder {
        static final int DEFAULT_LAYOUT = R.layout.app_recycler_item_cover;

        ImageView ivCover;
        TextView tvTitle;
        TextView tvProgress;
        CheckBox cb;

        CoverHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.app_iv_cover);
            tvTitle = itemView.findViewById(R.id.app_tv_title);
            tvProgress = itemView.findViewById(R.id.app_tv_progress);
            cb = itemView.findViewById(R.id.app_cb);
        }
    }

    static class CollectionHolder extends RecyclerView.ViewHolder {
        static final int DEFAULT_LAYOUT = R.layout.app_recycler_item_collection;

        BorderImageView ivCover1;
        BorderImageView ivCover2;
        BorderImageView ivCover3;
        BorderImageView ivCover4;
        TextView tvTitle;
        TextView tvCount;
        CheckBox cb;

        CollectionHolder(@NonNull View itemView) {
            super(itemView);
            ivCover1 = itemView.findViewById(R.id.app_iv_1);
            ivCover2 = itemView.findViewById(R.id.app_iv_2);
            ivCover3 = itemView.findViewById(R.id.app_iv_3);
            ivCover4 = itemView.findViewById(R.id.app_iv_4);
            tvTitle = itemView.findViewById(R.id.app_tv_title);
            tvCount = itemView.findViewById(R.id.app_tv_count);
            cb = itemView.findViewById(R.id.app_cb);
        }
    }
}
