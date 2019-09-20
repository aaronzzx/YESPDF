package com.aaron.yespdf.main;

import android.annotation.SuppressLint;
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

import com.aaron.base.image.DefaultOption;
import com.aaron.base.image.ImageLoader;
import com.aaron.yespdf.R;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.EmptyHolder;
import com.aaron.yespdf.common.bean.Collection;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.widgets.BorderImageView;
import com.blankj.utilcode.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class AllAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IOperationInterface {

    private static final int TYPE_EMPTY = 1;

    private Context context;

    private List<Collection> mCollections;

    private List<Collection> selectList = new ArrayList<>();
    private boolean selectMode;
    private SparseBooleanArray checkArray = new SparseBooleanArray();

    AllAdapter(List<Collection> list) {
        mCollections = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_EMPTY) {
            View itemView = inflater.inflate(R.layout.app_recycler_item_emptyview, parent, false);
            return new EmptyHolder(itemView);
        }
        View itemView = inflater.inflate(R.layout.app_recycler_item_collection, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (holder.cb.getVisibility() == View.VISIBLE) {
                Collection collection = mCollections.get(pos);
                boolean isChecked = !holder.cb.isChecked();
                holder.cb.setChecked(isChecked);
                if (holder.cb.isChecked()) {
                    selectList.add(collection);
                } else {
                    selectList.remove(collection);
                }
                checkArray.put(pos, isChecked);
                ((IFragmentInterface) context).onSelect(selectList, selectList.size() == mCollections.size());
            } else {
                String name = mCollections.get(pos).getName();
                ((IFragmentInterface) context).onTap(name);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            ((IFragmentInterface) context).startOperation();
            selectMode = true;
            notifyItemRangeChanged(0, getItemCount(), 0);
            return true;
        });
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) viewHolder;
            if (mCollections != null && !mCollections.isEmpty()) {
                Collection c = mCollections.get(position);
                List<PDF> pdfList = DBHelper.queryPDF(c.getName());
                int count = pdfList.size();

                holder.tvTitle.setText(c.getName());
                holder.tvCount.setText("共 " + count + " 本");
                if (count == 0) return;
                setCover(holder.ivCover1, pdfList.get(0).getCover());
                if (count == 1) return;
                setCover(holder.ivCover2, pdfList.get(1).getCover());
                if (count == 2) return;
                setCover(holder.ivCover3, pdfList.get(2).getCover());
                if (count == 3) return;
                setCover(holder.ivCover4, pdfList.get(3).getCover());
            }
            holder.cb.setVisibility(selectMode ? View.VISIBLE : View.GONE);
            if (selectMode) {
                holder.cb.setAlpha(1.0F);
                holder.cb.setScaleX(0.8F);
                holder.cb.setScaleY(0.8F);
                holder.cb.setChecked(checkArray.get(position));
            }
        } else if (viewHolder instanceof EmptyHolder) {
            EmptyHolder holder = (EmptyHolder) viewHolder;
            holder.itvEmpty.setVisibility(View.VISIBLE);
            holder.itvEmpty.setText(R.string.app_have_no_all);
            holder.itvEmpty.setIconTop(R.drawable.app_img_all);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(viewHolder, position, payloads);
        } else {
            if (viewHolder instanceof ViewHolder && position < getItemCount()) {
                ViewHolder holder = (ViewHolder) viewHolder;
                holder.cb.setVisibility(selectMode ? View.VISIBLE : View.GONE);
                if (selectMode) {
                    holder.cb.setAlpha(1.0F);
                    holder.cb.setScaleX(0.8F);
                    holder.cb.setScaleY(0.8F);
                    holder.cb.setChecked(checkArray.get(position));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mCollections.isEmpty()) {
            return 1;
        }
        return mCollections.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mCollections.isEmpty()) {
            return TYPE_EMPTY;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void cancel() {
        checkArray.clear();
        selectMode = false;
        selectList.clear();
        notifyItemRangeChanged(0, getItemCount(), 0);
    }

    @Override
    public void selectAll(boolean flag) {
        for (int i = 0; i < getItemCount(); i++) {
            checkArray.put(i, flag);
        }
        if (flag) {
            selectList.clear();
            for (int i = 0; i < getItemCount(); i++) {
                selectList.add(mCollections.get(i));
            }
        } else {
            selectList.clear();
        }
        ((IFragmentInterface) context).onSelect(selectList, flag);
        notifyItemRangeChanged(0, getItemCount(), 0);
    }

    private void setCover(ImageView ivCover, String path) {
        if (StringUtils.isEmpty(path)) return;
        ImageLoader.load(ivCover.getContext(), new DefaultOption.Builder(path).into(ivCover));
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private BorderImageView ivCover1;
        private BorderImageView ivCover2;
        private BorderImageView ivCover3;
        private BorderImageView ivCover4;
        private TextView tvTitle;
        private TextView tvCount;
        private CheckBox cb;

        ViewHolder(@NonNull View itemView) {
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
