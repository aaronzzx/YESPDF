package com.aaron.yespdf.about;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.image.DefaultOption;
import com.aaron.base.image.ImageLoader;
import com.aaron.yespdf.R;
import com.aaron.yespdf.common.UiManager;
import com.aaron.yespdf.common.utils.DialogUtils;
import com.blankj.utilcode.util.ImageUtils;

import java.util.List;

class MessageAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int INTRODUCE = 0;
    private static final int FEEDBACK = 1;
    private static final int SOURCE_CODE = 2;
    private static final int GITHUB = 3;
    private static final int RATE_APP = 4;
    private static final int GIFT = 5;

    private Activity activity;
    private Dialog giftDialog;
    private Dialog qrcodeDialog;

    private List<T> list;

    MessageAdapter(Activity activity, List<T> list) {
        this.activity = activity;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (giftDialog == null) {
            View view = LayoutInflater.from(context).inflate(R.layout.app_bottomdialog_gift, null);
            giftDialog = DialogUtils.createBottomSheetDialog(context, view);
            Button btnOpenQrcode = view.findViewById(R.id.app_btn_open_qrcode);
            Button btnGoWechat = view.findViewById(R.id.app_btn_go_wechat);
            btnOpenQrcode.setOnClickListener(v -> {
                qrcodeDialog.show();
            });
            btnGoWechat.setOnClickListener(v -> {
                giftDialog.dismiss();
                Bitmap bitmap = ImageUtils.drawable2Bitmap(context.getResources().getDrawable(R.drawable.app_img_qrcode));
                AboutUtils.copyImageToDevice(context, bitmap);
                AboutUtils.goWeChatScan(context);
                UiManager.showShort(R.string.app_wechat_scan_notice);
            });
        }
        if (qrcodeDialog == null) {
            qrcodeDialog = DialogUtils.createDialog(context, R.layout.app_dialog_qrcode);
            qrcodeDialog.setCanceledOnTouchOutside(true);
        }
        View view = inflater.inflate(R.layout.app_recycler_item_message, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(v -> {
            switch (holder.getAdapterPosition()) {
                case INTRODUCE:
                    Intent introduce = new Intent(Intent.ACTION_VIEW);
                    introduce.setData(Uri.parse(Info.MY_BLOG));
                    activity.startActivity(introduce);
                    break;
                case FEEDBACK:
                    String subject = Info.FEEDBACK_SUBJECT;
                    String text = Info.FEEDBACK_TEXT;
                    Intent sendMail = new Intent(Intent.ACTION_SENDTO);
                    sendMail.setData(Uri.parse(Info.MY_EMAIL));
                    sendMail.putExtra(Intent.EXTRA_SUBJECT, subject);
                    sendMail.putExtra(Intent.EXTRA_TEXT, text);
                    activity.startActivity(sendMail);
                    break;
                case SOURCE_CODE:
                    Intent sourceCode = new Intent(Intent.ACTION_VIEW);
                    sourceCode.setData(Uri.parse(Info.SOURCE_CODE));
                    activity.startActivity(sourceCode);
                    break;
                case GITHUB:
                    Intent github = new Intent(Intent.ACTION_VIEW);
                    github.setData(Uri.parse(Info.MY_GITHUB));
                    activity.startActivity(github);
                    break;
                case RATE_APP:
                    AboutUtils.openCoolApk(context, context.getPackageName());
                    break;
                case GIFT:
                    giftDialog.show();
                    break;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) list.get(position);
        ImageLoader.load(activity, new DefaultOption.Builder(message.getIconId())
                .into(((ViewHolder) holder).icon));
        ((ViewHolder) holder).title.setText(message.getTitle());
    }

    @Override
    public int getItemCount() {
        return list.size();
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