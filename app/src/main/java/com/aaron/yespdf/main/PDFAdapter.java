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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.image.DefaultOption;
import com.aaron.base.image.ImageLoader;
import com.aaron.yespdf.R;
import com.aaron.yespdf.common.App;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.EmptyHolder;
import com.aaron.yespdf.common.Settings;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.event.AllEvent;
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
class PDFAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IOperationInterface {

    private static final int TYPE_EMPTY = 1;

    private Context context;

    private RecentPDFEvent recentPDFEvent;
    private AllEvent allEvent;
    private boolean recent;

    private List<PDF> pdfList;
    private List<PDF> selectList = new ArrayList<>();
    private List<CheckBox> cbList = new ArrayList<>();
    private SparseBooleanArray checkArray = new SparseBooleanArray();
    private SparseBooleanArray visibilityArray = new SparseBooleanArray();

    PDFAdapter(List<PDF> pdfList, boolean recent) {
        this.pdfList = pdfList;
        recentPDFEvent = new RecentPDFEvent(false);
        allEvent = new AllEvent();
        this.recent = recent;
    }

    @SuppressLint("SimpleDateFormat")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_EMPTY) {
            View itemView = inflater.inflate(R.layout.app_recycler_item_emptyview, parent, false);
            return new EmptyHolder(itemView);
        }
        View itemView = inflater.inflate(R.layout.app_recycler_item_cover, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        cbList.add(holder.cb);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (holder.cb.getVisibility() == View.VISIBLE) {
                PDF pdf = pdfList.get(pos);
                holder.cb.setChecked(!holder.cb.isChecked());
                checkArray.delete(pos);
                checkArray.put(pos, holder.cb.isChecked());
                if (holder.cb.isChecked()) {
                    selectList.add(pdf);
                } else {
                    selectList.remove(pdf);
                }
                ((IFragmentInterface) context).onSelect(selectList, selectList.size() == pdfList.size());
            } else {
                PDF pdf = pdfList.get(pos);
                long cur = System.currentTimeMillis();
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                pdf.setLatestRead(Long.parseLong(TimeUtils.millis2String(cur, df)));
                DBHelper.updatePDF(pdf);
                DBHelper.insertRecent(pdf);
                PreviewActivity.start(context, pdf);
                EventBus.getDefault().post(recentPDFEvent);
                EventBus.getDefault().post(allEvent);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            ((IFragmentInterface) context).startOperation();
            for (CheckBox cb : cbList) {
                cb.setVisibility(View.VISIBLE);
            }
            visibilityArray.put(0, true);
            return true;
        });
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Context context = viewHolder.itemView.getContext();
        if (viewHolder instanceof ViewHolder && position < getItemCount()) {
            ViewHolder holder = (ViewHolder) viewHolder;
            PDF pdf = pdfList.get(position);
            String cover = pdf.getCover();
            String bookName = pdf.getName();
            holder.tvTitle.setText(bookName);
            holder.tvProgress.setText(context.getString(R.string.app_already_read) + pdf.getProgress());
            ImageLoader.load(holder.itemView.getContext(), new DefaultOption.Builder(cover)
                    .into(holder.ivCover));
            if (visibilityArray.get(0)) {
                holder.cb.setVisibility(View.VISIBLE);
                boolean isChecked = checkArray.get(position);
                holder.cb.setChecked(isChecked);
            } else {
                holder.cb.setVisibility(View.GONE);
            }

        } else if (viewHolder instanceof EmptyHolder) {
            EmptyHolder holder = (EmptyHolder) viewHolder;
            holder.itvEmpty.setVisibility(View.VISIBLE);
            holder.itvEmpty.setText(R.string.app_have_no_recent);
            holder.itvEmpty.setIconTop(R.drawable.app_img_recent);
        }
    }

    @Override
    public int getItemCount() {
        if (pdfList.isEmpty()) {
            return 1;
        } else if (recent) {
            String[] array = App.getContext().getResources().getStringArray(R.array.max_recent_count);
            String infinite = array[array.length - 1];
            String maxRecent = Settings.getMaxRecentCount();
            if (!maxRecent.equals(infinite)) {
                int count = Integer.parseInt(maxRecent);
                if (count <= pdfList.size()) {
                    return count;
                }
                return pdfList.size();
            }
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
        for (CheckBox cb : cbList) {
            cb.setChecked(false);
            cb.setVisibility(View.GONE);
        }
        checkArray.clear();
        visibilityArray.clear();
        selectList.clear();
    }

    @Override
    public void selectAll(boolean flag) {
        for (CheckBox cb : cbList) {
            cb.setChecked(flag);
        }
        if (flag) {
            selectList.clear();
            selectList.addAll(pdfList);
        } else {
            selectList.clear();
        }
        ((IFragmentInterface) context).onSelect(selectList, selectList.size() == pdfList.size());
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
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
