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
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.preview.PreviewActivity;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class CollectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<PDF> mPDFList;

    CollectionAdapter(List<PDF> pdfList) {
        mPDFList = pdfList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.app_recycler_item_pdf, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            PreviewActivity.start(context, mPDFList.get(pos).getPath());
        });
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        PDF pdf = mPDFList.get(position);

        String cover = pdf.getCover();
        String bookName = pdf.getName();
        holder.tvTitle.setText(bookName);
        holder.tvProgress.setText("已读 " + pdf.getProgress());
        ImageLoader.load(holder.itemView.getContext(), new DefaultOption.Builder(cover)
                .into(holder.ivCover));
    }

    @Override
    public int getItemCount() {
        return mPDFList.size();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCover;
        private TextView tvTitle;
        private TextView tvProgress;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.app_iv_cover);
            tvTitle = itemView.findViewById(R.id.app_tv_title);
            tvProgress = itemView.findViewById(R.id.app_tv_progress);
        }
    }
}
