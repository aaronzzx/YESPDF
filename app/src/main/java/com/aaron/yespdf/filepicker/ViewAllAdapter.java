package com.aaron.yespdf.filepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.EmptyHolder;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.TimeUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class ViewAllAdapter extends AbstractAdapter implements Filterable {

    private static final int TYPE_EMPTY = 1;
    private static final int TYPE_CONTENT = 2;

    private Context context;
    private Callback callback;

    private boolean inverse;

    private List<File> filterList;
    private List<File> fileList;
    private List<String> importedList;

    private List<String> selectList = new ArrayList<>();

    private SparseBooleanArray checkArray = new SparseBooleanArray();

    ViewAllAdapter(List<File> fileList, List<String> importedList, Callback callback) {
        this.fileList = fileList;
        this.importedList = importedList;
        this.callback = callback;
        this.filterList = fileList;
    }

    void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint == null) {
                    filterList = fileList;
                    FilterResults results = new FilterResults();
                    results.values = filterList;
                    return results;
                }
                String keyword = constraint.toString();
                if (StringUtils.isEmpty(keyword)) {
                    filterList = fileList;
                } else {
                    filterList = new ArrayList<>();
                    for (File file : fileList) {
                        boolean contains = file.getName().contains(keyword) != inverse;
                        if (contains) {
                            filterList.add(file);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filterList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filterList = (List<File>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_EMPTY) {
            View itemView = inflater.inflate(R.layout.app_recycler_item_emptyview, parent, false);
            return new EmptyHolder(itemView);
        }
        View itemView = inflater.inflate(R.layout.app_recycler_item_filepicker, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            File file = filterList.get(pos);
            if (file.isDirectory()) {
                callback.onDirTap(file.getAbsolutePath());
            } else {
                if (holder.cb.isEnabled() && !importedList.contains(file.getAbsolutePath())) {
                    holder.cb.setChecked(!holder.cb.isChecked());
                    if (holder.cb.isChecked() && !selectList.contains(file.getAbsolutePath())) {
                        selectList.add(file.getAbsolutePath());
                    } else {
                        selectList.remove(file.getAbsolutePath());
                    }
                }
                checkArray.put(pos, holder.cb.isChecked());
                callback.onSelectResult(selectList, fileCount());
            }
        });
        return holder;
    }

    @SuppressLint("SetTextI18n,SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof EmptyHolder) {
            EmptyHolder holder = (EmptyHolder) viewHolder;
            holder.itvEmpty.setVisibility(View.VISIBLE);
            holder.itvEmpty.setText(R.string.app_have_no_file);
            holder.itvEmpty.setIconTop(R.drawable.app_img_file);
        } else {
            ViewHolder holder = (ViewHolder) viewHolder;
            File file = filterList.get(position);
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
                if (!importedList.isEmpty() && importedList.contains(file.getAbsolutePath())) {
                    holder.cb.setEnabled(false);
                    holder.cb.setPadding(0, 0, ConvertUtils.dp2px(2), 0);
                } else {
                    holder.cb.setEnabled(true);
                }
                holder.cb.setChecked(checkArray.get(position));
                holder.tvTitle.setText(name);
                holder.tvDescription.setText(desc + lastModified);
            }
            holder.tvTitle.setText(name);
            holder.tvDescription.setText(desc + lastModified);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(viewHolder, position, payloads);
        } else {
            if (viewHolder instanceof ViewHolder) {
                ViewHolder holder = (ViewHolder) viewHolder;
                if (holder.cb.getVisibility() == View.VISIBLE) {
                    holder.cb.setChecked(checkArray.get(position));
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (filterList == null || filterList.isEmpty()) {
            return TYPE_EMPTY;
        }
        return TYPE_CONTENT;
    }

    @Override
    public int getItemCount() {
        if (filterList == null || filterList.isEmpty()) {
            return 1;
        }
        return filterList.size();
    }

    @Override
    public void selectAll(boolean selectAll) {
        checkArray.clear();
        selectList.clear();
        for (File file : filterList) {
            if (file.isFile() && !importedList.contains(file.getAbsolutePath())) {
                checkArray.put(filterList.indexOf(file), selectAll);
            }
            if (selectAll && file.isFile() && !importedList.contains(file.getAbsolutePath())) {
                selectList.add(file.getAbsolutePath());
            }
        }

//        selectList.clear();
//        if (selectAll) {
////            for (int i = 0; i < getItemCount(); i++) {
////                File file = filterList.get(i);
////                if (file.isFile() && !importedList.contains(file.getAbsolutePath())) {
////                    selectList.add(filterList.get(i).getAbsolutePath());
////                }
////            }
//            for (File file : filterList) {
//                if (file.isFile() && !importedList.contains(file.getAbsolutePath())) {
//                    selectList.add(file.getAbsolutePath());
//                }
//            }
//        }
        callback.onSelectResult(selectList, fileCount());
        notifyItemRangeChanged(0, getItemCount(), 0);
    }

    @Override
    boolean reset() {
        checkArray.clear();
        selectList.clear();
        for (File file : filterList) {
            if (file.isFile() && !importedList.contains(file.getAbsolutePath())) {
                return true;
            }
        }
        return false;
    }

    private int fileCount() {
        int count = 0;
        for (File file : filterList) {
            if (file.isFile() && !importedList.contains(file.getAbsolutePath())) {
                count++;
            }
        }
        return count;
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

    interface Callback {
        void onDirTap(String dirPath);

        void onSelectResult(List<String> pathList, int total);
    }
}
