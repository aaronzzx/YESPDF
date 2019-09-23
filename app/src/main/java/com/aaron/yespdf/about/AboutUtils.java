package com.aaron.yespdf.about;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.UiManager;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.UriUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
final class AboutUtils {

    static void openCoolApk(Context context, String selfPkg) {
        if (!StringUtils.isEmpty(selfPkg)) {
            try {
                String coolApk = "com.coolapk.market";
                Uri uri = Uri.parse("market://details?id=" + selfPkg);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage(coolApk);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                UiManager.showShort(R.string.app_have_no_install_coolapk);
            }
        }
    }

    static void goWeChatScan(Context context) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"));
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
            intent.setFlags(335544320);
            intent.setAction("android.intent.action.VIEW");
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            UiManager.showShort(R.string.app_have_no_install_wechat);
        }
    }

    static void copyImageToDevice(Context context, Bitmap bitmap) {
        ThreadUtils.getIoPool().submit(() -> {
            File file = new File(PathUtils.getExternalAppCachePath(), "yespdf-gift.jpg");
            if (file.exists()) file.delete();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                        MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = UriUtils.file2Uri(file);
            intent.setData(uri);
            context.sendBroadcast(intent);
        });
    }

    private AboutUtils() {
    }
}
