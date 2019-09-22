package com.aaron.yespdf.main;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.image.DefaultOption;
import com.aaron.base.image.ImageLoader;
import com.aaron.yespdf.R;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.EmptyHolder;
import com.aaron.yespdf.common.bean.Collection;
import com.aaron.yespdf.common.bean.PDF;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class AllAdapter extends AbstractAdapter<Collection> {

    private FragmentManager fm;

    AllAdapter(ICommInterface<Collection> commInterface, FragmentManager fm, List<Collection> sourceList) {
        super(commInterface, sourceList);
        this.fm = fm;
    }

    @NonNull
    @Override
    RecyclerView.ViewHolder createHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(CollectionHolder.DEFAULT_LAYOUT, parent, false);
        return new CollectionHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    void bindHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        LogUtils.e("喵喵喵");
        if (viewHolder instanceof CollectionHolder) {
            CollectionHolder holder = (CollectionHolder) viewHolder;
            if (!sourceList.isEmpty()) {
                Collection c = sourceList.get(position);
                List<PDF> pdfList = DBHelper.queryPDF(c.getName());
                int count = pdfList.size();

                holder.tvTitle.setText(c.getName());
                holder.tvCount.setText(context.getString(R.string.app_total) + count + context.getString(R.string.app_count));
                setVisibility(holder, count);
                if (count == 0) return;
                setCover(holder.ivCover1, pdfList.get(0).getCover());
                if (count == 1) return;
                setCover(holder.ivCover2, pdfList.get(1).getCover());
                if (count == 2) return;
                setCover(holder.ivCover3, pdfList.get(2).getCover());
                if (count == 3) return;
                setCover(holder.ivCover4, pdfList.get(3).getCover());
            }
            handleCheckBox(holder.cb, position);
        } else if (viewHolder instanceof EmptyHolder) {
            EmptyHolder holder = (EmptyHolder) viewHolder;
            holder.itvEmpty.setVisibility(View.VISIBLE);
            holder.itvEmpty.setText(R.string.app_have_no_all);
            holder.itvEmpty.setIconTop(R.drawable.app_img_all);
        }
    }

    @Override
    void bindHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            bindHolder(viewHolder, position);
        } else {
            if (viewHolder instanceof CollectionHolder && position < getItemCount()) {
                CollectionHolder holder = (CollectionHolder) viewHolder;
                handleCheckBox(holder.cb, position);
            }
        }
    }

    @Override
    int itemCount() {
        return sourceList.size();
    }

    @Override
    void onTap(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CollectionHolder) {
            CollectionHolder holder = (CollectionHolder) viewHolder;
            if (holder.cb.getVisibility() == View.VISIBLE) {
                Collection collection = sourceList.get(position);
                boolean isChecked = !holder.cb.isChecked();
                holder.cb.setChecked(isChecked);
                if (holder.cb.isChecked()) {
                    selectList.add(collection);
                } else {
                    selectList.remove(collection);
                }
                checkArray.put(position, isChecked);
                commInterface.onSelect(selectList, selectList.size() == getItemCount());
            } else {
                DialogFragment df = CollectionFragment.newInstance(sourceList.get(position).getName());
                df.show(fm, "");
            }
        }
    }

    @Override
    void checkCurrent(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CollectionHolder) {
            CollectionHolder holder = (CollectionHolder) viewHolder;
            holder.cb.setChecked(true);
        }
    }

    private void setVisibility(CollectionHolder holder, int count) {
        holder.ivCover1.setVisibility(count >= 1 ? View.VISIBLE : View.INVISIBLE);
        holder.ivCover2.setVisibility(count >= 2 ? View.VISIBLE : View.INVISIBLE);
        holder.ivCover3.setVisibility(count >= 3 ? View.VISIBLE : View.INVISIBLE);
        holder.ivCover4.setVisibility(count >= 4 ? View.VISIBLE : View.INVISIBLE);
    }

    private void setCover(ImageView ivCover, String path) {
        if (!StringUtils.isEmpty(path)) {
            ImageLoader.load(context, new DefaultOption.Builder(path).into(ivCover));
        }
    }
}
