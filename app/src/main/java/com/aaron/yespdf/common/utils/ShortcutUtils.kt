package com.aaron.yespdf.common.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.aaron.yespdf.R
import com.aaron.yespdf.common.App
import com.aaron.yespdf.common.bean.Shortcut
import com.aaron.yespdf.main.MainActivity
import com.aaron.yespdf.main.SearchActivity


/**
 * @author aaronzzxup@gmail.com
 * @since 2020/6/14
 */
object ShortcutUtils {

    private const val ID_SHORTCUT_SEARCH = "yespdf_shortcut_search"

    private var rank: Int = 1

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return
        }

        (context.getSystemService(Context.SHORTCUT_SERVICE) as? ShortcutManager)?.apply {
            dynamicShortcuts.apply {
                if (isEmpty()) {
                    add(createShortcutInfo(context, Shortcut(
                            SearchActivity::class.java,
                            ID_SHORTCUT_SEARCH,
                            App.getContext().resources.getString(R.string.app_search),
                            App.getContext().resources.getString(R.string.app_search),
                            R.drawable.app_ic_shortcut_search
                    )))
                    dynamicShortcuts = this
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPinnedShortcut(context: Context, shortcut: Shortcut, needMainIntent: Boolean = true): Boolean {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        shortcutManager ?: return false
        if (shortcutManager.isRequestPinShortcutSupported) {
            val (target, id, shortLabel, longLabel, icon, extraKey, extraValue) = shortcut
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
            }
            val targetIntent = Intent(context, target).apply {
                action = Intent.ACTION_VIEW
                putExtra(extraKey, extraValue)
            }
            val pinShortcutInfo = ShortcutInfo.Builder(context, id).run {
                setShortLabel(shortLabel)
                setLongLabel(longLabel)
                setIcon(Icon.createWithResource(context, icon))
                if (needMainIntent)
                    setIntents(arrayOf(mainIntent, targetIntent))
                else setIntent(targetIntent)
                build()
            }
            val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)
            val successCallback = PendingIntent.getBroadcast(context, 0,
                    pinnedShortcutCallbackIntent, 0)
            return shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.intentSender)
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun createShortcutInfo(context: Context, shortcut: Shortcut): ShortcutInfo {
        val (target, id, shortLabel, longLabel, icon, extraKey, extraValue) = shortcut
        val targetIntent = Intent(Intent.ACTION_MAIN, Uri.EMPTY, context, target).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (null != extraKey && extraValue != null) {
                putExtra(extraKey, extraValue)
            }
        }
        val mainIntent = Intent(context, MainActivity::class.java).apply { action = Intent.ACTION_VIEW }
        return ShortcutInfo.Builder(context, id).run {
            setShortLabel(shortLabel)
            setLongLabel(longLabel)
            setIcon(Icon.createWithResource(context, icon))
            setIntents(arrayOf(mainIntent, targetIntent)) //当按下back时，将转到MainActivity
            setRank(if (ID_SHORTCUT_SEARCH == id) 0 else rank++)
            build()
        }
    }
}