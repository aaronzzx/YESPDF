package com.aaron.yespdf.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.image.DefaultOption;
import com.aaron.base.image.ImageLoader;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.R;
import com.aaron.yespdf.common.bean.Collection;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.widgets.BorderImageView;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.StringUtils;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class AllAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Collection> mCollections;

    AllAdapter(List<Collection> list) {
        mCollections = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.app_recycler_item_collection, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            String name = mCollections.get(pos).getName();
            ((AllAdapterComm) context).onTap(name);
        });
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        switch (mCollections.size()) {
            case 1:
                holder.ivCover1.setBackground(new ColorDrawable(Color.WHITE));
                holder.ivCover1.setBorderRadius(ConvertUtils.dp2px(1));
                holder.ivCover2.clearBorder();
                holder.ivCover3.clearBorder();
                holder.ivCover4.clearBorder();
                break;
            case 2:
                holder.ivCover1.setBackground(new ColorDrawable(Color.WHITE));
                holder.ivCover1.setBorderRadius(ConvertUtils.dp2px(1));
                holder.ivCover2.setBackground(new ColorDrawable(Color.WHITE));
                holder.ivCover2.setBorderRadius(ConvertUtils.dp2px(1));
                holder.ivCover3.clearBorder();
                holder.ivCover4.clearBorder();
                break;
            case 3:
                holder.ivCover1.setBackground(new ColorDrawable(Color.WHITE));
                holder.ivCover1.setBorderRadius(ConvertUtils.dp2px(1));
                holder.ivCover2.setBackground(new ColorDrawable(Color.WHITE));
                holder.ivCover2.setBorderRadius(ConvertUtils.dp2px(1));
                holder.ivCover3.setBackground(new ColorDrawable(Color.WHITE));
                holder.ivCover3.setBorderRadius(ConvertUtils.dp2px(1));
                holder.ivCover4.clearBorder();
                break;
            default:
                holder.ivCover1.setBackground(new ColorDrawable(Color.WHITE));
                holder.ivCover1.setBorderRadius(ConvertUtils.dp2px(1));
                holder.ivCover2.setBackground(new ColorDrawable(Color.WHITE));
                holder.ivCover2.setBorderRadius(ConvertUtils.dp2px(1));
                holder.ivCover3.setBackground(new ColorDrawable(Color.WHITE));
                holder.ivCover3.setBorderRadius(ConvertUtils.dp2px(1));
                holder.ivCover4.setBackground(new ColorDrawable(Color.WHITE));
                holder.ivCover4.setBorderRadius(ConvertUtils.dp2px(1));
                break;
        }
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

    @Override
    public int getItemCount() {
        return mCollections.size();
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
