package com.aaron.yespdf.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.image.DefaultOption;
import com.aaron.base.image.ImageLoader;
import com.aaron.yespdf.R;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.EmptyHolder;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.event.RecentPDFEvent;
import com.aaron.yespdf.preview.PreviewActivity;
import com.blankj.utilcode.util.TimeUtils;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
abstract class AbstractPDFAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IOperationInterface {

    private static final int TYPE_EMPTY = 1;

    protected Context context;

    private RecentPDFEvent recentPDFEvent;

    protected List<PDF> pdfList;
    protected List<PDF> selectList = new ArrayList<>();

    private boolean selectMode;
    private SparseBooleanArray checkArray = new SparseBooleanArray();

    AbstractPDFAdapter(List<PDF> pdfList) {
        this.pdfList = pdfList;
        recentPDFEvent = new RecentPDFEvent(false);
    }

    @LayoutRes
    abstract int itemView();

    abstract void startOperation();

    abstract void onSelect(List<PDF> list, boolean isSelectAll);

    @SuppressLint("SimpleDateFormat")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (this.context == null) {
            this.context = parent.getContext();
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_EMPTY) {
            View itemView = inflater.inflate(R.layout.app_recycler_item_emptyview, parent, false);
            return new EmptyHolder(itemView);
        }
        View itemView = inflater.inflate(itemView(), parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (holder.cb.getVisibility() == View.VISIBLE) {
                PDF pdf = pdfList.get(pos);
                boolean isChecked = !holder.cb.isChecked();
                holder.cb.setChecked(isChecked);
                if (holder.cb.isChecked()) {
                    selectList.add(pdf);
                } else {
                    selectList.remove(pdf);
                }
                checkArray.put(pos, isChecked);
                onSelect(selectList, selectList.size() == getItemCount());
            } else {
                PDF pdf = pdfList.get(pos);
                long cur = System.currentTimeMillis();
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                pdf.setLatestRead(Long.parseLong(TimeUtils.millis2String(cur, df)));
                DBHelper.updatePDF(pdf);
                DBHelper.insertRecent(pdf);
                PreviewActivity.start(context, pdf);
                EventBus.getDefault().post(recentPDFEvent);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            startOperation();
            selectMode = true;
            notifyItemRangeChanged(0, getItemCount(), 0);
            return true;
        });
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ViewHolder && position < getItemCount()) {
            ViewHolder holder = (ViewHolder) viewHolder;
            PDF pdf = pdfList.get(position);
            String cover = pdf.getCover();
            String bookName = pdf.getName();
            holder.tvTitle.setText(bookName);
            holder.tvProgress.setText(context.getString(R.string.app_already_read) + pdf.getProgress());
            ImageLoader.load(context, new DefaultOption.Builder(cover).into(holder.ivCover));
            holder.cb.setVisibility(selectMode ? View.VISIBLE : View.GONE);
            if (selectMode) {
                holder.cb.setAlpha(1.0F);
                holder.cb.setScaleX(0.8F);
                holder.cb.setScaleY(0.8F);
                holder.cb.setChecked(checkArray.get(position));
            }

        } else if (viewHolder instanceof EmptyHolder) {
            EmptyHolder holder = (EmptyHolder) viewHolder;
            holder.itvEmpty.setVisibility(View.VISIBLE);
            holder.itvEmpty.setText(R.string.app_have_no_recent);
            holder.itvEmpty.setIconTop(R.drawable.app_img_recent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(viewHolder, position, payloads);
        } else {
            if (viewHolder instanceof ViewHolder && position < getItemCount()) {
                ViewHolder holder = (ViewHolder) viewHolder;
                holder.cb.setVisibility(selectMode ? View.VISIBLE : View.GONE);
                if (selectMode) {
                    holder.cb.setAlpha(1.0F);
                    holder.cb.setScaleX(0.8F);
                    holder.cb.setScaleY(0.8F);
                    holder.cb.setChecked(checkArray.get(position));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (pdfList.isEmpty()) {
            return 1;
        }
        return pdfList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (pdfList.isEmpty()) {
            return TYPE_EMPTY;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void cancel() {
        checkArray.clear();
        selectMode = false;
        selectList.clear();
        notifyItemRangeChanged(0, getItemCount(), 0);
    }

    @Override
    public void selectAll(boolean flag) {
        for (int i = 0; i < getItemCount(); i++) {
            checkArray.put(i, flag);
        }
        if (flag) {
            selectList.clear();
            for (int i = 0; i < getItemCount(); i++) {
                selectList.add(pdfList.get(i));
            }
        } else {
            selectList.clear();
        }
        onSelect(selectList, flag);
        notifyItemRangeChanged(0, getItemCount(), 0);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCover;
        private TextView tvTitle;
        private TextView tvProgress;
        private CheckBox cb;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.app_iv_cover);
            tvTitle = itemView.findViewById(R.id.app_tv_title);
            tvProgress = itemView.findViewById(R.id.app_tv_progress);
            cb = itemView.findViewById(R.id.app_cb);
        }
    }
}
