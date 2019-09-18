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
class SelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IAdapterInterface {

    private List<File> mFileList;
    private List<String> mImportedList;

    private List<String> mSelectList = new ArrayList<>();
    private List<CheckBox> mCbList = new ArrayList<>();

    private boolean isSelectAll;
    private Context mContext;

    SelectAdapter(List<File> fileList, List<String> imported) {
        mFileList = fileList;
        mImportedList = imported;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.app_recycler_item_filepicker, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        mCbList.add(holder.cb); // 将所有 CheckBox 收集起来方便全选
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            File file = mFileList.get(pos);
            if (file.isDirectory()) {
                ((IActivityInterface) mContext).onDirTap(file.getAbsolutePath());
            } else {
                if (holder.cb.isEnabled() && !mImportedList.contains(file.getAbsolutePath())) {
                    holder.cb.setChecked(!holder.cb.isChecked());
                    if (holder.cb.isChecked() && !mSelectList.contains(file.getAbsolutePath())) {
                        mSelectList.add(file.getAbsolutePath());
                    } else {
                        mSelectList.remove(file.getAbsolutePath());
                    }
                }
                ((IActivityInterface) mContext).onSelectResult(mSelectList, fileCount());
            }
        });
        return holder;
    }

    @SuppressLint("SetTextI18n,SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Context context = viewHolder.itemView.getContext();
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
            //判断是否已导入
            if (mImportedList != null && !mImportedList.isEmpty()) {
                if (mImportedList.contains(file.getAbsolutePath())) {
                    holder.cb.setEnabled(false);
                    holder.cb.setPadding(0, 0, ConvertUtils.dp2px(2), 0);
                }
            }
            // 为了避免复用混乱
            if (holder.cb.isEnabled()) {
                if (isSelectAll) {
                    holder.cb.setChecked(true);
                } else {
                    if (mSelectList.contains(file.getAbsolutePath())) {
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
        return mFileList.size();
    }

    @Override
    public void selectAll(boolean flag) {
        isSelectAll = flag;
        // 如果有一个 CheckBox 是启用状态，就还能使用全选
        boolean selectAllDisable = true;
        for (CheckBox cb : mCbList) {
            if (cb.isEnabled()) {
                selectAllDisable = false;
                break;
            }
        }
        if (selectAllDisable) {
            ((IActivityInterface) mContext).getViewSelectAll().setEnabled(false);
            return;
        }
        // 全选
        if (isSelectAll) {
            mSelectList.clear();
            for (CheckBox cb : mCbList) {
                if (cb.isEnabled()) cb.setChecked(true);
            }
            for (File file : mFileList) {
                if (file.isFile() && !mImportedList.contains(file.getAbsolutePath())) {
                    mSelectList.add(file.getAbsolutePath());
                }
            }
        } else {
            mSelectList.clear();
            for (CheckBox cb : mCbList) {
                if (cb.isEnabled()) cb.setChecked(false);
            }
        }
        ((IActivityInterface) mContext).getViewSelectAll().setSelected(mSelectList.size() == fileCount());
        ((IActivityInterface) mContext).onSelectResult(mSelectList, fileCount());
    }

    @Override
    public List<String> selectResult() {
        return mSelectList;
    }

    @Override
    public void reset() {
        isSelectAll = false;
        mSelectList.clear();
        checkSelectAllStatus();
    }

    private int fileCount() {
        int count = 0;
        for (File file : mFileList) {
            if (file.isFile()) count++;
        }
        return count;
    }

    private void checkSelectAllStatus() {
        boolean enable = false;
        for (File file : mFileList) {
            if (file.isFile() && !mImportedList.contains(file.getAbsolutePath())) {
                enable = true;
                break;
            }
        }
        if (mContext != null) {
            View view = ((IActivityInterface) mContext).getViewSelectAll();
            if (view != null) {
                view.setEnabled(enable);
            }
        }
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
