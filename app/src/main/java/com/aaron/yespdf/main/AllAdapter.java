package com.aaron.yespdf.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.image.DefaultOption;
import com.aaron.base.image.ImageLoader;
import com.aaron.yespdf.PdfUtils;
import com.aaron.yespdf.R;

import java.io.IOException;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class AllAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Bitmap b1;
    private Bitmap b2;
    private Bitmap b3;
    private Bitmap b4;

    AllAdapter() {
        try {
            b1 = PdfUtils.pdfToBitmap("/storage/emulated/0/Android#Java/Java/算法(第4版).pdf", 0);
            b2 = PdfUtils.pdfToBitmap("/storage/emulated/0/Android#Java/Java/大话设计模式.pdf", 0);
            b3 = PdfUtils.pdfToBitmap("/storage/emulated/0/Android#Java/Java/Effective Java 第二版 中文版.pdf", 0);
            b4 = PdfUtils.pdfToBitmap("/storage/emulated/0/Android#Java/Java/Java 编程的逻辑.pdf", 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.app_recycler_item_all, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> ((Communicable) context).onTap());
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.tvTitle.setText("Java");
        holder.tvCount.setText("共 16 本");
        setCover(holder.ivCover1, b1);
        setCover(holder.ivCover2, b2);
        setCover(holder.ivCover3, b3);
        setCover(holder.ivCover4, b4);
    }

    @Override
    public int getItemCount() {
        return 3 * 3;
    }

    private void setCover(ImageView ivCover, Bitmap bitmap) {
        ImageLoader.load(ivCover.getContext(), new DefaultOption.Builder(bitmap)
                .into(ivCover));
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCover1;
        private ImageView ivCover2;
        private ImageView ivCover3;
        private ImageView ivCover4;
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
