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
import com.aaron.yespdf.preview.PreviewActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class CollectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> mPathList;
    private List<Bitmap> mCoverList = new ArrayList<>();

    CollectionAdapter(List<String> pathList) {
        mPathList = pathList;
        for (String path : mPathList) {
            try {
                Bitmap bitmap = PdfUtils.pdfToBitmap(path, 0);
                mCoverList.add(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            PreviewActivity.start(context, mPathList.get(pos));
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        Bitmap cover = mCoverList.get(position);
        String path = mPathList.get(position);
        String bookName = path.substring(path.lastIndexOf("/") + 1, path.length() - 4);
        holder.tvTitle.setText(bookName);
        holder.tvProgress.setText("已读 25.3%");
        ImageLoader.load(holder.itemView.getContext(), new DefaultOption.Builder(cover)
                .into(holder.ivCover));
    }

    @Override
    public int getItemCount() {
        return mPathList.size();
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
