package com.aaron.yespdf.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class AllAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_EMPTY = 1;

    private List<Collection> mCollections;

    AllAdapter(List<Collection> list) {
        mCollections = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_EMPTY) {
            View itemView = inflater.inflate(R.layout.app_recycler_item_emptyview, parent, false);
            return new EmptyHolder(itemView);
        }
        View itemView = inflater.inflate(R.layout.app_recycler_item_collection, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            String name = mCollections.get(pos).getName();
            ((IAllAdapterComm) context).onTap(name);
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
        } else if (viewHolder instanceof EmptyHolder) {
            EmptyHolder holder = (EmptyHolder) viewHolder;
            holder.itvEmpty.setVisibility(View.VISIBLE);
            holder.itvEmpty.setText(R.string.app_have_no_all);
            holder.itvEmpty.setIconTop(R.drawable.app_img_all);
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

    private void setCover(ImageView ivCover, String path) {
        if (StringUtils.isEmpty(path)) return;
        ImageLoader.load(ivCover.getContext(), new DefaultOption.Builder(path)
                .into(ivCover));
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private BorderImageView ivCover1;
        private BorderImageView ivCover2;
        private BorderImageView ivCover3;
        private BorderImageView ivCover4;
        private TextView tvTitle;
        private TextView tvCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover1 = itemView.findViewById(R.id.app_iv_1);
            ivCover2 = itemView.findViewById(R.id.app_iv_2);
            ivCover3 = itemView.findViewById(R.id.app_iv_3);
            ivCover4 = itemView.findViewById(R.id.app_iv_4);
            tvTitle = itemView.findViewById(R.id.app_tv_title);
            tvCount = itemView.findViewById(R.id.app_tv_count);
        }
    }
}
