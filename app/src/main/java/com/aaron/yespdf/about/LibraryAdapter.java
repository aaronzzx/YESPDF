package com.aaron.yespdf.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;

import java.util.List;

class LibraryAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<T> mList;

    LibraryAdapter(Context context, List<T> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_recycler_item_library, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(v -> {
            switch (holder.getAdapterPosition()) {
                case 0:
                    Intent androidTreeView = new Intent(Intent.ACTION_VIEW);
                    androidTreeView.setData(Uri.parse("https://github.com/bmelnychuk/AndroidTreeView"));
                    mContext.startActivity(androidTreeView);
                    break;
                case 1:
                    Intent realtimeBlurView = new Intent(Intent.ACTION_VIEW);
                    realtimeBlurView.setData(Uri.parse("https://github.com/mmin18/RealtimeBlurView"));
                    mContext.startActivity(realtimeBlurView);
                    break;
                case 2:
                    Intent parallaxBackLayout = new Intent(Intent.ACTION_VIEW);
                    parallaxBackLayout.setData(Uri.parse("https://github.com/anzewei/ParallaxBackLayout"));
                    mContext.startActivity(parallaxBackLayout);
                    break;
                case 3:
                    Intent greenDao = new Intent(Intent.ACTION_VIEW);
                    greenDao.setData(Uri.parse("https://github.com/greenrobot/greenDAO"));
                    mContext.startActivity(greenDao);
                    break;
                case 4:
                    Intent butterKnife = new Intent(Intent.ACTION_VIEW);
                    butterKnife.setData(Uri.parse("https://github.com/JakeWharton/butterknife"));
                    mContext.startActivity(butterKnife);
                    break;
                case 5:
                    Intent glide = new Intent(Intent.ACTION_VIEW);
                    glide.setData(Uri.parse("https://github.com/bumptech/glide"));
                    mContext.startActivity(glide);
                    break;
                case 6:
                    Intent statusBarUtil = new Intent(Intent.ACTION_VIEW);
                    statusBarUtil.setData(Uri.parse("https://github.com/laobie/StatusBarUtil"));
                    mContext.startActivity(statusBarUtil);
                    break;
                case 7:
                    Intent eventBus = new Intent(Intent.ACTION_VIEW);
                    eventBus.setData(Uri.parse("https://github.com/greenrobot/EventBus"));
                    mContext.startActivity(eventBus);
                    break;
                case 8:
                    Intent rxJava = new Intent(Intent.ACTION_VIEW);
                    rxJava.setData(Uri.parse("https://github.com/ReactiveX/RxJava"));
                    mContext.startActivity(rxJava);
                    break;
                case 9:
                    Intent rxAndroid = new Intent(Intent.ACTION_VIEW);
                    rxAndroid.setData(Uri.parse("https://github.com/ReactiveX/RxAndroid"));
                    mContext.startActivity(rxAndroid);
                    break;
                case 10:
                    Intent androidPdfViewer = new Intent(Intent.ACTION_VIEW);
                    androidPdfViewer.setData(Uri.parse("https://github.com/barteksc/AndroidPdfViewer"));
                    mContext.startActivity(androidPdfViewer);
                    break;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Library library = (Library) mList.get(position);
        ((ViewHolder) holder).name.setText(library.getName());
        ((ViewHolder) holder).author.setText(library.getAuthor());
        ((ViewHolder) holder).introduce.setText(library.getIntroduce());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView name;
        TextView author;
        TextView introduce;

        /**
         * @param view 子项布局的最外层布局，即父布局。
         */
        ViewHolder(View view) {
            super(view);
            itemView = view;
            name = view.findViewById(R.id.app_tv_name);
            author = view.findViewById(R.id.app_tv_author);
            introduce = view.findViewById(R.id.app_tv_detail);
        }
    }
}
