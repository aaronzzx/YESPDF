package com.aaron.yespdf.filepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.TimeUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class DeprecateSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements DeprecateIAdapterInterface {

    private List<File> mFileList;
    private List<String> mImportedList;
    private List<String> mPathList = new ArrayList<>();
    private List<CheckBox> mCbList = new ArrayList<>();

    private boolean isSelectAll = false;
    private Context mContext;

    DeprecateSelectAdapter(List<File> fileList, List<String> imported) {
        mFileList = fileList;
        mImportedList = imported;
    }

    @Override
    public void init() {
        isSelectAll = false;
        mPathList.clear();
    }

    @Override
    public void selectAll() {
        boolean isAllDisable = true;
        for (CheckBox cb : mCbList) {
            if (cb.isEnabled()) {
                isAllDisable = false;
                break;
            }
        }
        if (isAllDisable) {
            ((DeprecateIActivityInterface) mContext).onSelectResult(mPathList, fileCount(), true);
            return;
        }

        isSelectAll = !isSelectAll;
        if (isSelectAll) {
            for (CheckBox cb : mCbList) {
                if (cb.isEnabled()) cb.setChecked(true);
            }
            mPathList.clear();
            for (File file : mFileList) {
                if (file.isFile() && !mImportedList.contains(file.getAbsolutePath())) {
                    mPathList.add(file.getAbsolutePath());
                }
            }
        } else {
            for (CheckBox cb : mCbList) {
                if (cb.isEnabled()) cb.setChecked(false);
            }
            mPathList.clear();
        }
        ((DeprecateIActivityInterface) mContext).onSelectResult(mPathList, fileCount(), false);
    }

    private int fileCount() {
        int count = 0;
        for (File file : mFileList) {
            if (file.isFile()) count++;
        }
        return count;
    }

    private void disableSelectAll() {
        for (File file : mFileList) {
            if (file.isFile() && !mImportedList.contains(file.getAbsolutePath())) {
                return;
            }
        }
        ((DeprecateIActivityInterface) mContext).onSelectResult(mPathList, fileCount(), true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.app_recycler_item_filepicker, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        mCbList.add(holder.cb);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            File file = mFileList.get(pos);
            boolean isDir = file.isDirectory();
            if (isDir) {
                ((DeprecateIActivityInterface) mContext).onDirTap(file.getAbsolutePath());
            } else {
                if (!holder.cb.isEnabled()) return;
                if (!mImportedList.contains(file.getAbsolutePath())) {
                    holder.cb.setChecked(!holder.cb.isChecked());
                    if (holder.cb.isChecked() && !mPathList.contains(file.getAbsolutePath())) {
                        mPathList.add(file.getAbsolutePath());
                    } else {
                        mPathList.remove(file.getAbsolutePath());
                    }
                    ((DeprecateIActivityInterface) mContext).onSelectResult(mPathList, fileCount(), false);
                }
            }
        });
        return holder;
    }

    @SuppressLint("SetTextI18n,SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (mFileList == null) return;
        Context context = viewHolder.itemView.getContext();
        disableSelectAll();
        ViewHolder holder = (ViewHolder) viewHolder;
        File file = mFileList.get(position);
        String name = file.getName();
        String desc = 0 + context.getString(R.string.app_item);
        String lastModified = TimeUtils.millis2String(file.lastModified(), new SimpleDateFormat("yyyy/MM/dd HH:mm"));
        if (file.isDirectory()) {
            holder.ivIcon.setImageResource(R.drawable.app_ic_folder_yellow_24dp);
            holder.ivNext.setVisibility(View.VISIBLE);
            holder.cb.setVisibility(View.GONE);
            File[] files = file.listFiles(new FileFilterImpl());
            if (files != null) desc = files.length + context.getString(R.string.app_item);
        } else {
            // 大小 MB 留小数点后一位
            String size = String.valueOf((double) file.length() / 1024 / 1024);
            size = size.substring(0, size.indexOf(".") + 2);
            desc = size + " MB  -  ";
            holder.ivNext.setVisibility(View.GONE);
            holder.cb.setVisibility(View.VISIBLE);
            holder.cb.setPadding(0, 0, 0, 0);
            if (file.getName().endsWith(".pdf")) {
                holder.ivIcon.setImageResource(R.drawable.app_ic_pdf);
            }
            if (mImportedList != null && !mImportedList.isEmpty()) {
                if (mImportedList.contains(file.getAbsolutePath())) {
                    holder.cb.setEnabled(false);
                    holder.cb.setPadding(0, 0, ConvertUtils.dp2px(2), 0);
                }
            }
            if (holder.cb.isEnabled()) {
                if (isSelectAll) {
                    holder.cb.setChecked(true);
                } else {
                    if (mPathList.contains(file.getAbsolutePath())) {
                        holder.cb.setChecked(true);
                    } else {
                        holder.cb.setChecked(false);
                    }
                }
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
        private CheckBox cb;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.app_iv_icon);
            tvTitle = itemView.findViewById(R.id.app_tv_title);
            tvDescription = itemView.findViewById(R.id.app_tv_description);
            ivNext = itemView.findViewById(R.id.app_iv_next);
            cb = itemView.findViewById(R.id.app_cb);
        }
    }
}
