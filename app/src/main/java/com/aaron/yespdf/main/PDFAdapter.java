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
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.EmptyHolder;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.event.AllEvent;
import com.aaron.yespdf.common.event.RecentPDFEvent;
import com.aaron.yespdf.preview.PreviewActivity;
import com.blankj.utilcode.util.TimeUtils;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class PDFAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_EMPTY = 1;

    private RecentPDFEvent mRecentPDFEvent;
    private AllEvent mAllEvent;
    private List<PDF> mPDFList;

    PDFAdapter(List<PDF> pdfList) {
        mPDFList = pdfList;
        mRecentPDFEvent = new RecentPDFEvent(false);
        mAllEvent = new AllEvent();
    }

    @SuppressLint("SimpleDateFormat")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_EMPTY) {
            View itemView = inflater.inflate(R.layout.app_recycler_item_emptyview, parent, false);
            return new EmptyHolder(itemView);
        }
        View itemView = inflater.inflate(R.layout.app_recycler_item_pdf, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            PDF pdf = mPDFList.get(pos);
            long cur = System.currentTimeMillis();
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            pdf.setLatestRead(Long.parseLong(TimeUtils.millis2String(cur, df)));
            DBHelper.updatePDF(pdf);
            DBHelper.insertRecent(pdf);
            PreviewActivity.start(context, pdf);
            EventBus.getDefault().post(mRecentPDFEvent);
            EventBus.getDefault().post(mAllEvent);
        });
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Context context = viewHolder.itemView.getContext();
        if (viewHolder instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) viewHolder;
            PDF pdf = mPDFList.get(position);
            String cover = pdf.getCover();
            String bookName = pdf.getName();
            holder.tvTitle.setText(bookName);
            holder.tvProgress.setText(context.getString(R.string.app_already_read) + pdf.getProgress());
            ImageLoader.load(holder.itemView.getContext(), new DefaultOption.Builder(cover)
                    .into(holder.ivCover));
        } else if (viewHolder instanceof EmptyHolder) {
            EmptyHolder holder = (EmptyHolder) viewHolder;
            holder.itvEmpty.setVisibility(View.VISIBLE);
            holder.itvEmpty.setText(R.string.app_have_no_recent);
            holder.itvEmpty.setIconTop(R.drawable.app_img_recent);
        }
    }

    @Override
    public int getItemCount() {
        if (mPDFList.isEmpty()) {
            return 1;
        }
        return mPDFList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mPDFList.isEmpty()) {
            return TYPE_EMPTY;
        }
        return super.getItemViewType(position);
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
