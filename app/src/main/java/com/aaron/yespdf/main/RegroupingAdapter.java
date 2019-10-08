package com.aaron.yespdf.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.image.DefaultOption;
import com.aaron.base.image.ImageLoader;
import com.aaron.yespdf.R;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.bean.Collection;
import com.aaron.yespdf.common.bean.PDF;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.StringUtils;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class RegroupingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ADD_COLLECTION = 1;

    private Context context;

    private List<Collection> collectionList;
    private Callback callback;

    RegroupingAdapter(List<Collection> collectionList, Callback callback) {
        this.collectionList = collectionList;
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_ADD_COLLECTION) {
            View add = inflater.inflate(AbstractAdapter.CoverHolder.DEFAULT_LAYOUT, parent, false);
            AbstractAdapter.CoverHolder holder = new AbstractAdapter.CoverHolder(add);
            holder.tvProgress.setVisibility(View.GONE);
            holder.cb.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> callback.onAddNewGroup());
            return holder;
        }
        View view = inflater.inflate(AbstractAdapter.CollectionHolder.DEFAULT_LAYOUT, parent, false);
        AbstractAdapter.CollectionHolder holder = new AbstractAdapter.CollectionHolder(view);
        holder.itemView.setOnClickListener(v -> callback.onAddToGroup(holder.tvTitle.getText().toString()));
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof AbstractAdapter.CoverHolder) {
            AbstractAdapter.CoverHolder holder = (AbstractAdapter.CoverHolder) viewHolder;
            holder.tvTitle.setText(R.string.app_add_new_group);
            holder.ivCover.setScaleType(ImageView.ScaleType.FIT_XY);
            holder.ivCover.setImageResource(R.drawable.app_bg_add);
            ViewGroup.LayoutParams lp = holder.ivCover.getLayoutParams();
            lp.height = ConvertUtils.dp2px(152);
            holder.ivCover.requestLayout();
        } else {
            AbstractAdapter.CollectionHolder holder = (AbstractAdapter.CollectionHolder) viewHolder;
            if (!collectionList.isEmpty()) {
                Collection c = collectionList.get(position - 1);
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
        }
    }

    private void setVisibility(AbstractAdapter.CollectionHolder holder, int count) {
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

    @Override
    public int getItemCount() {
        return 1 + collectionList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_ADD_COLLECTION;
        }
        return super.getItemViewType(position);
    }

    interface Callback {
        void onAddNewGroup();

        void onAddToGroup(String dir);
    }
}
