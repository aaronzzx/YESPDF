package com.github.barteksc.pdfviewer.util

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.StringUtils
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
        }
    }

    fun copyImageToDevice(
            context: Context,
            bitmap: Bitmap,
            savePath: String = "${PathUtils.getExternalAppCachePath()}/yespdf-gift.jpg"
    ) {
        val file = File(savePath)
        file.mkdirs()
        if (file.exists()) file.delete()
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        notifyMedia(context, file.absolutePath, "com.aaron.yespdf.fileprovider")
    }

    private fun notifyMedia(context: Context, path: String, authority: String) {
        try {
            // 通知相册更新
            val file = File(path)
            MediaStore.Images.Media.insertImage(context.contentResolver, BitmapFactory.decodeFile(file.absolutePath), file.name, null)
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val uri = getUri(context, authority, file)
            intent.data = uri
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getUri(context: Context, authority: String, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(context, authority, file)
        } else {
            Uri.fromFile(file)
        }
    }
}