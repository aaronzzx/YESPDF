package com.aaron.yespdf.about

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.aaron.yespdf.R
import com.aaron.yespdf.common.UiManager
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.UriUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @author Aaron aaronzzxup@gmail.com
 */
internal object AboutUtils {
    fun openCoolApk(context: Context, selfPkg: String) {
        if (!StringUtils.isEmpty(selfPkg)) {
            try {
                val coolApk = "com.coolapk.market"
                val uri = Uri.parse("market://details?id=$selfPkg")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage(coolApk)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                UiManager.showShort(R.string.app_have_no_install_coolapk)
            }
        }
    }

    @SuppressLint("WrongConstant")
    fun goWeChatScan(context: Context) {
        try {
            val intent = Intent()
            intent.component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true)
            intent.flags = 335544320
            intent.action = "android.intent.action.VIEW"
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            UiManager.showShort(R.string.app_have_no_install_wechat)
        }
    }

    fun copyImageToDevice(context: Context, bitmap: Bitmap) {
        ThreadUtils.getIoPool().submit {
            val file = File(PathUtils.getExternalAppCachePath(), "yespdf-gift.jpg")
            if (file.exists()) file.delete()
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    if (fos != null) {
                        fos.close()
                        MediaStore.Images.Media.insertImage(context.contentResolver, file.absolutePath, file.name, null)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val uri = UriUtils.file2Uri(file)
            intent.data = uri
            context.sendBroadcast(intent)
        }
    }
}