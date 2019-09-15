package com.aaron.yespdf.preview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.widgets.ImageTextView;
import com.blankj.utilcode.util.TimeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class BookmarkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_EMPTY = 1;

    private List<Bookmark> mBookmarks;

    private DateFormat mDateFormat;

    @SuppressLint("SimpleDateFormat")
    BookmarkAdapter(List<Bookmark> list) {
        mBookmarks = list;
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_EMPTY) {
            View itemView = inflater.inflate(R.layout.app_recycler_item_emptyview, parent, false);
            return new EmptyHolder(itemView);
        }
        View itemView = inflater.inflate(R.layout.app_recycler_item_bookmark, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            Bookmark bk = mBookmarks.get(pos);
            ((IActivityComm) context).onJumpTo(bk.getPageId());
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (!mBookmarks.isEmpty() && viewHolder instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) viewHolder;
            Bookmark bk = mBookmarks.get(position);
            holder.tvTitle.setText(bk.getTitle());
            holder.tvPageId.setText(String.valueOf(bk.getPageId() + 1));
            String time = TimeUtils.millis2String(bk.getTime(), mDateFormat);
            holder.tvTime.setText(time);
        } else if (viewHolder instanceof EmptyHolder) {
            EmptyHolder holder = (EmptyHolder) viewHolder;
            holder.itvEmpty.setVisibility(View.VISIBLE);
            holder.itvEmpty.setText(R.string.app_have_no_add_bookmark);
            holder.itvEmpty.setIconTop(R.drawable.app_ic_bookmark_emptyview);
        }
    }

    @Override
    public int getItemCount() {
        if (mBookmarks.isEmpty()) {
            return 1;
        }
        return mBookmarks.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mBookmarks.isEmpty()) {
            return TYPE_EMPTY;
        }
        return super.getItemViewType(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPageId, tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.app_tv_title);
            tvPageId = itemView.findViewById(R.id.app_tv_page_id);
            tvTime = itemView.findViewById(R.id.app_tv_time);
        }
    }

    static class EmptyHolder extends RecyclerView.ViewHolder {
        ImageTextView itvEmpty;

        EmptyHolder(@NonNull View itemView) {
            super(itemView);
            itvEmpty = itemView.findViewById(R.id.app_itv_placeholder);
        }
    }
}
