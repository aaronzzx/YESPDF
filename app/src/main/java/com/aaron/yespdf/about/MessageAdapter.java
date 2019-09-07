package com.aaron.yespdf.about;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.image.DefaultOption;
import com.aaron.base.image.ImageLoader;
import com.aaron.yespdf.BuildConfig;
import com.aaron.yespdf.R;
import com.blankj.utilcode.util.ScreenUtils;

import java.util.List;
import java.util.Locale;

class MessageAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int INTRODUCE = 0;
    private static final int FEEDBACK = 1;
    private static final int SOURCE_CODE = 2;
    private static final int GITHUB = 3;
    private static final int GIFT = 4;

    private Activity mActivity;
    private List<T> mList;

    MessageAdapter(Activity activity, List<T> list) {
        mActivity = activity;
        mList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_recycler_item_message, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(v -> {
            switch (holder.getAdapterPosition()) {
                case INTRODUCE:
                    Intent introduce = new Intent(Intent.ACTION_VIEW);
                    introduce.setData(Uri.parse(Info.MY_BLOG));
                    mActivity.startActivity(introduce);
                    break;
                case FEEDBACK:
                    String subject = Info.FEEDBACK_SUBJECT;
                    String text = Info.FEEDBACK_TEXT;
                    Intent sendMail = new Intent(Intent.ACTION_SENDTO);
                    sendMail.setData(Uri.parse(Info.MY_EMAIL));
                    sendMail.putExtra(Intent.EXTRA_SUBJECT, subject);
                    sendMail.putExtra(Intent.EXTRA_TEXT, text);
                    mActivity.startActivity(sendMail);
                    break;
                case SOURCE_CODE:
                    Intent sourceCode = new Intent(Intent.ACTION_VIEW);
                    sourceCode.setData(Uri.parse(Info.SOURCE_CODE));
                    mActivity.startActivity(sourceCode);
                    break;
                case GITHUB:
                    Intent github = new Intent(Intent.ACTION_VIEW);
                    github.setData(Uri.parse(Info.MY_GITHUB));
                    mActivity.startActivity(github);
                    break;
                case GIFT:

                    break;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) mList.get(position);
        ImageLoader.load(mActivity, new DefaultOption.Builder(message.getIconId())
                .into(((ViewHolder) holder).icon));
        ((ViewHolder) holder).title.setText(message.getTitle());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        ImageView icon;
        TextView title;

        /**
         * @param view 子项布局的最外层布局，即父布局。
         */
        ViewHolder(View view) {
            super(view);
            itemView = view;
            icon = view.findViewById(R.id.app_iv_icon);
            title = view.findViewById(R.id.app_tv_text);
        }
    }
}