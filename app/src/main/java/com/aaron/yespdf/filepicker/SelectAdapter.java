//package com.aaron.yespdf.filepicker;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.util.SparseBooleanArray;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CheckBox;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.aaron.yespdf.R;
//import com.blankj.utilcode.util.ConvertUtils;
//import com.blankj.utilcode.util.TimeUtils;
//
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author Aaron aaronzzxup@gmail.com
// */
//class SelectAdapter extends AbstractAdapter {
//
//    private Context context;
//
//    private List<File> fileList;
//    private List<String> importedList;
//
//    private List<String> selectList = new ArrayList<>();
//
//    private SparseBooleanArray checkArray = new SparseBooleanArray();
//
//    SelectAdapter(List<File> fileList, List<String> importedList) {
//        this.fileList = fileList;
//        this.importedList = importedList;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (context == null) context = parent.getContext();
//        LayoutInflater inflater = LayoutInflater.from(context);
//        View itemView = inflater.inflate(R.layout.app_recycler_item_filepicker, parent, false);
//        ViewHolder holder = new ViewHolder(itemView);
//        holder.itemView.setOnClickListener(v -> {
//            int pos = holder.getAdapterPosition();
//            File file = fileList.get(pos);
//            if (file.isDirectory()) {
//                ((SelectActivity) context).onDirTap(file.getAbsolutePath());
//            } else {
//                if (holder.cb.isEnabled() && !importedList.contains(file.getAbsolutePath())) {
//                    holder.cb.setChecked(!holder.cb.isChecked());
//                    if (holder.cb.isChecked() && !selectList.contains(file.getAbsolutePath())) {
//                        selectList.add(file.getAbsolutePath());
//                    } else {
//                        selectList.remove(file.getAbsolutePath());
//                    }
//                }
//                checkArray.put(pos, holder.cb.isChecked());
//                ((SelectActivity) context).onSelectResult(selectList, fileCount());
//            }
//        });
//        return holder;
//    }
//
//    @SuppressLint("SetTextI18n,SimpleDateFormat")
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
//        ViewHolder holder = (ViewHolder) viewHolder;
//        File file = fileList.get(position);
//        String name = file.getName();
//        String desc = 0 + context.getString(R.string.app_item);
//        String lastModified = TimeUtils.millis2String(file.lastModified(), new SimpleDateFormat("yyyy/MM/dd HH:mm"));
//        if (file.isDirectory()) {
//            holder.ivIcon.setImageResource(R.drawable.app_ic_folder_yellow_24dp);
//            holder.ivNext.setVisibility(View.VISIBLE);
//            holder.cb.setVisibility(View.GONE);
//            File[] files = file.listFiles(new FileFilterImpl());
//            if (files != null) desc = files.length + context.getString(R.string.app_item);
//        } else {
//            // 大小 MB 留小数点后一位
//            String size = String.valueOf((double) file.length() / 1024 / 1024);
//            size = size.substring(0, size.indexOf(".") + 2);
//            desc = size + " MB  -  ";
//            holder.ivNext.setVisibility(View.GONE);
//            holder.cb.setVisibility(View.VISIBLE);
//            holder.cb.setPadding(0, 0, 0, 0);
//            if (file.getName().endsWith(".pdf")) {
//                holder.ivIcon.setImageResource(R.drawable.app_ic_pdf);
//            }
//            //判断是否已导入
//            if (!importedList.isEmpty() && importedList.contains(file.getAbsolutePath())) {
//                holder.cb.setEnabled(false);
//                holder.cb.setPadding(0, 0, ConvertUtils.dp2px(2), 0);
//            } else {
//                holder.cb.setEnabled(true);
//            }
//            holder.cb.setChecked(checkArray.get(position));
//        }
//        holder.tvTitle.setText(name);
//        holder.tvDescription.setText(desc + lastModified);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
//        if (payloads.isEmpty()) {
//            super.onBindViewHolder(viewHolder, position, payloads);
//        } else {
//            if (viewHolder instanceof ViewHolder) {
//                ViewHolder holder = (ViewHolder) viewHolder;
//                if (holder.cb.getVisibility() == View.VISIBLE) {
//                    holder.cb.setChecked(checkArray.get(position));
//                }
//            }
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return fileList.size();
//    }
//
//    @Override
//    public void selectAll(boolean selectAll) {
//        checkArray.clear();
//        for (File file : fileList) {
//            if (file.isFile() && !importedList.contains(file.getAbsolutePath())) {
//                checkArray.put(fileList.indexOf(file), selectAll);
//            }
//        }
//        selectList.clear();
//        if (selectAll) {
//            for (int i = 0; i < getItemCount(); i++) {
//                if (fileList.get(i).isFile()) {
//                    selectList.add(fileList.get(i).getAbsolutePath());
//                }
//            }
//        }
//        ((SelectActivity) context).onSelectResult(selectList, fileCount());
//        notifyItemRangeChanged(0, getItemCount(), 0);
//    }
//
//    @Override
//    boolean reset() {
//        checkArray.clear();
//        for (File file : fileList) {
//            if (file.isFile() && !importedList.contains(file.getAbsolutePath())) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private int fileCount() {
//        int count = 0;
//        for (File file : fileList) {
//            if (file.isFile()) count++;
//        }
//        return count;
//    }
//
//    private static class ViewHolder extends RecyclerView.ViewHolder {
//        private ImageView ivIcon;
//        private TextView tvTitle;
//        private TextView tvDescription;
//        private ImageView ivNext;
//        private CheckBox cb;
//
//        ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            ivIcon = itemView.findViewById(R.id.app_iv_icon);
//            tvTitle = itemView.findViewById(R.id.app_tv_title);
//            tvDescription = itemView.findViewById(R.id.app_tv_description);
//            ivNext = itemView.findViewById(R.id.app_iv_next);
//            cb = itemView.findViewById(R.id.app_cb);
//        }
//    }
//}
