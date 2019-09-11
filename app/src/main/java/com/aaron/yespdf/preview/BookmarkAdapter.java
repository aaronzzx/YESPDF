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
import com.blankj.utilcode.util.TimeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    private List<Bookmark> mBookmarks;

    private DateFormat mDateFormat;

    @SuppressLint("SimpleDateFormat")
    BookmarkAdapter(List<Bookmark> list) {
        mBookmarks = list;
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!mBookmarks.isEmpty()) {
            Bookmark bk = mBookmarks.get(position);
            holder.tvTitle.setText(bk.getTitle());
            holder.tvPageId.setText(String.valueOf(bk.getPageId() + 1));
            String time = TimeUtils.millis2String(bk.getTime(), mDateFormat);
            holder.tvTime.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return mBookmarks.size();
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
}
