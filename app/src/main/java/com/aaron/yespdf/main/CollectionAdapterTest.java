package com.aaron.yespdf.main;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
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
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class CollectionAdapterTest extends AbstractAdapter<PDF> {

    private RecentPDFEvent recentPDFEvent;

    CollectionAdapterTest(ICommInterface<PDF> commInterface, List<PDF> sourceList) {
        super(commInterface, sourceList);
        recentPDFEvent = new RecentPDFEvent();
    }

    @NonNull
    @Override
    RecyclerView.ViewHolder createHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(CoverHolder.DEFAULT_LAYOUT, parent, false);
        return new CoverHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    void bindHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CoverHolder && position < getItemCount()) {
            CoverHolder holder = (CoverHolder) viewHolder;
            PDF pdf = sourceList.get(position);
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
    void bindHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            bindHolder(viewHolder, position);
        } else {
            if (viewHolder instanceof CoverHolder && position < getItemCount()) {
                CoverHolder holder = (CoverHolder) viewHolder;
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
    int itemCount() {
        return sourceList.size();
    }

    @Override
    void onTap(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CoverHolder) {
            CoverHolder holder = (CoverHolder) viewHolder;
            if (holder.cb.getVisibility() == View.VISIBLE) {
                PDF pdf = sourceList.get(position);
                boolean isChecked = !holder.cb.isChecked();
                holder.cb.setChecked(isChecked);
                if (holder.cb.isChecked()) {
                    selectList.add(pdf);
                } else {
                    selectList.remove(pdf);
                }
                checkArray.put(position, isChecked);
                commInterface.onSelect(selectList, selectList.size() == getItemCount());
            } else {
                PDF pdf = sourceList.get(position);
                long cur = System.currentTimeMillis();
                @SuppressLint("SimpleDateFormat")
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                pdf.setLatestRead(Long.parseLong(TimeUtils.millis2String(cur, df)));
                DBHelper.updatePDF(pdf);
                DBHelper.insertRecent(pdf);
                PreviewActivity.start(context, pdf);
                EventBus.getDefault().post(recentPDFEvent);
                holder.itemView.postDelayed(() -> {
                    ((DialogFragment) commInterface).dismiss();
                }, 400);
            }
        }
    }
}
