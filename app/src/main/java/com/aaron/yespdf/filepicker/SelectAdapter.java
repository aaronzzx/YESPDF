package com.aaron.yespdf.filepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.impl.OnClickListenerImpl;
import com.aaron.yespdf.R;
import com.blankj.utilcode.util.TimeUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class SelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<File> mFileList;

    SelectAdapter(List<File> fileList) {
        mFileList = fileList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.app_recycler_item_file, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                int pos = holder.getAdapterPosition();
                ((Communicable) context).onTap(v, mFileList.get(pos).getAbsolutePath());
            }
        });
        return holder;
    }

    @SuppressLint("SetTextI18n,SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (mFileList == null) return;
        ViewHolder holder = (ViewHolder) viewHolder;
        File file = mFileList.get(position);
        String name = file.getName();
        String desc = "0项  -  ";
        String lastModified = TimeUtils.millis2String(file.lastModified(), new SimpleDateFormat("yyyy-MM-dd HH:mm"));
        if (file.isDirectory()) {
            holder.ivIcon.setImageResource(R.drawable.app_ic_folder_yellow_24dp);
            File[] files = file.listFiles(new FileFilterImpl());
            if (files != null) desc = files.length + "项  -  ";
        } else {
            // 大小 MB 留小数点后一位
            String size = String.valueOf((double) file.length() / 1024 / 1024);
            size = size.substring(0, size.indexOf(".") + 2);
            desc = size + " MB  -  ";
            holder.ivNext.setVisibility(View.INVISIBLE);
            if (file.getName().endsWith(".pdf")) {
                holder.ivIcon.setImageResource(R.drawable.app_ic_pdf_red_24dp);
            }
        }
        holder.tvTitle.setText(name);
        holder.tvDescription.setText(desc + lastModified);
    }

    @Override
    public int getItemCount() {
        if (mFileList != null) {
            return mFileList.size();
        }
        return 0;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvTitle;
        private TextView tvDescription;
        private ImageView ivNext;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.app_iv_icon);
            tvTitle = itemView.findViewById(R.id.app_tv_title);
            tvDescription = itemView.findViewById(R.id.app_tv_description);
            ivNext = itemView.findViewById(R.id.app_iv_next);
        }
    }
}
