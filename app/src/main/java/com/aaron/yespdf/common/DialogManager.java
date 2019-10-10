package com.aaron.yespdf.common;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.bean.Cover;
import com.aaron.yespdf.common.utils.DialogUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class DialogManager {

    public static Dialog createLoadingDialog(Context context) {
        return DialogUtils.createDialog(context, R.layout.app_dialog_loading);
    }

    public static Dialog createQrcodeDialog(Context context) {
        return DialogUtils.createDialog(context, R.layout.app_dialog_qrcode);
    }

    public static Dialog createAlertDialog(Context context, AlertDialogCallback callback) {
        View view = LayoutInflater.from(context.getApplicationContext())
                .inflate(R.layout.app_dialog_alert, null);
        TextView tvTitle = view.findViewById(R.id.app_tv_title);
        TextView tvContent = view.findViewById(R.id.app_tv_content);
        Button btn = view.findViewById(R.id.app_btn);
        callback.onTitle(tvTitle);
        callback.onContent(tvContent);
        callback.onButton(btn);
        Dialog dialog = DialogUtils.createDialog(context, view);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static Dialog createInputDialog(Context context, InputDialogCallback callback) {
        View view = LayoutInflater.from(context.getApplicationContext())
                .inflate(R.layout.app_dialog_input, null);
        TextView tvTitle = view.findViewById(R.id.app_tv_title);
        EditText etInput = view.findViewById(R.id.app_et_input);
        Button btnCancel = view.findViewById(R.id.app_btn_cancel);
        Button btnConfirm = view.findViewById(R.id.app_btn_confirm);
        callback.onTitle(tvTitle);
        callback.onInput(etInput);
        callback.onLeft(btnCancel);
        callback.onRight(btnConfirm);
        Dialog dialog = DialogUtils.createDialog(context, view);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static Dialog createDoubleBtnDialog(Context context, DoubleBtnDialogCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
        View dialogView = inflater.inflate(R.layout.app_dialog_double_btn, null);
        Dialog dialog = DialogUtils.createDialog(context, dialogView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        TextView tvTitle = dialogView.findViewById(R.id.app_tv_title);
        TextView tvContent = dialogView.findViewById(R.id.app_tv_content);
        Button btnLeft = dialogView.findViewById(R.id.app_btn_left);
        Button btnRight = dialogView.findViewById(R.id.app_btn_right);
        callback.onTitle(tvTitle);
        callback.onContent(tvContent);
        callback.onLeft(btnLeft);
        callback.onRight(btnRight);
        return dialog;
    }

    public static BottomSheetDialog createDeleteDialog(Context context, DeleteDialogCallback callback) {
        View view = LayoutInflater.from(context.getApplicationContext())
                .inflate(R.layout.app_bottomdialog_delete, null);
        TextView tvDeleteDescription = view.findViewById(R.id.app_tv_description);
        Button btnCancel = view.findViewById(R.id.app_btn_cancel);
        Button btnDelete = view.findViewById(R.id.app_btn_delete);
        callback.onContent(tvDeleteDescription);
        callback.onLeft(btnCancel);
        callback.onRight(btnDelete);
        return DialogUtils.createBottomSheetDialog(context, view);
    }

    public static BottomSheetDialog createGiftDialog(Context context, GiftDialogCallback callback) {
        View view = LayoutInflater.from(context.getApplicationContext())
                .inflate(R.layout.app_bottomdialog_gift, null);
        Button btnOpenQrcode = view.findViewById(R.id.app_btn_open_qrcode);
        Button btnGoWechat = view.findViewById(R.id.app_btn_go_wechat);
        callback.onLeft(btnOpenQrcode);
        callback.onRight(btnGoWechat);
        return DialogUtils.createBottomSheetDialog(context, view, R.style.AppRegroupingAnim, true);
    }

    public static BottomSheetDialog createImportDialog(Context context, ImportDialogCallback callback) {
        View view = LayoutInflater.from(context.getApplicationContext())
                .inflate(R.layout.app_bottomdialog_import, null);
        EditText etInput = view.findViewById(R.id.app_et_input);
        Button btnImportExist = view.findViewById(R.id.app_btn_import_exist);
        Button btnBaseFolder = view.findViewById(R.id.app_btn_base_folder);
        Button btnConfirm = view.findViewById(R.id.app_btn_confirm);
        callback.onInput(etInput);
        callback.onLeft(btnImportExist);
        callback.onCenter(btnBaseFolder);
        callback.onRight(btnConfirm);
        return DialogUtils.createBottomSheetDialog(context, view);
    }

    public static BottomSheetDialog createGroupingDialog(Context context, boolean enableAddNew, GroupingAdapter.Callback callback) {
        View view = LayoutInflater.from(context.getApplicationContext())
                .inflate(R.layout.app_bottomdialog_grouping, null);
        RecyclerView rv = view.findViewById(R.id.app_rv_group);

        rv.addItemDecoration(new XGridDecoration());
        rv.addItemDecoration(new YGridDecoration());
        GridLayoutManager lm = new GridLayoutManager(context, 3);
        rv.setLayoutManager(lm);
        List<Cover> list = DataManager.getCoverList();
        RecyclerView.Adapter adapter = new GroupingAdapter(lm, list, callback, enableAddNew);
        rv.setAdapter(adapter);
        return DialogUtils.createBottomSheetDialog(context, view, R.style.AppRegroupingAnim, true);
    }

    public static BottomSheetDialog createScanDialog(Context context, ScanDialogCallback callback) {
        View view = LayoutInflater.from(context.getApplicationContext())
                .inflate(R.layout.app_bottomdialog_scan, null);
        TextView tvScanCount = view.findViewById(R.id.app_tv_scan_count);
        TextView tvPdfCount = view.findViewById(R.id.app_tv_pdf_count);
        Button btnStopScan = view.findViewById(R.id.app_btn_stop_scan);
        callback.onTitle(tvScanCount);
        callback.onContent(tvPdfCount);
        callback.onButton(btnStopScan);
        BottomSheetDialog scanDialog = DialogUtils.createBottomSheetDialog(context, view);
        scanDialog.setCanceledOnTouchOutside(false);
        scanDialog.setCancelable(false);
        return scanDialog;
    }

    public interface AlertDialogCallback {
        void onTitle(TextView tv);

        void onContent(TextView tv);

        void onButton(Button btn);
    }

    public interface InputDialogCallback {
        void onTitle(TextView tv);

        void onInput(EditText et);

        void onLeft(Button btn);

        void onRight(Button btn);
    }

    public interface DoubleBtnDialogCallback {
        void onTitle(TextView tv);

        void onContent(TextView tv);

        void onLeft(Button btn);

        void onRight(Button btn);
    }

    public interface DeleteDialogCallback {
        void onContent(TextView tv);

        void onLeft(Button btn);

        void onRight(Button btn);
    }

    public interface GiftDialogCallback {
        void onLeft(Button btn);

        void onRight(Button btn);
    }

    public interface ImportDialogCallback {
        void onInput(EditText et);

        void onLeft(Button btn);

        void onCenter(Button btn);

        void onRight(Button btn);
    }

    public interface ScanDialogCallback {
        void onTitle(TextView tv);

        void onContent(TextView tv);

        void onButton(Button btn);
    }

    private DialogManager() {}
}
