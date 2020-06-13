package com.aaron.yespdf.common

import com.blankj.utilcode.util.SPStaticUtils

/**
 * 全局设置
 * <p>
 * @author aaronzzxup@gmail.com
 * @since 2020/6/13
 */
object Settings {

    private const val SP_LOCK_LANDSCAPE = "SP_LOCK_LANDSCAPE"
    private const val SP_MAX_RECENT_COUNT = "SP_MAX_RECENT_COUNT"
    private const val SP_SWIPE_HORIZONTAL = "SP_SWIPE_HORIZONTAL"
    private const val SP_NIGHT_MODE = "SP_NIGHT_MODE"
    private const val SP_VOLUME_CONTROL = "SP_VOLUME_CONTROL"
    private const val SP_SHOW_STATUS_BAR = "SP_SHOW_STATUS_BAR"
    private const val SP_SCROLL_LEVEL = "SP_SCROLL_LEVEL"
    private const val SP_CLICK_FLIP_PAGE = "SP_CLICK_FLIP_PAGE"
    private const val SP_KEEP_SCREEN_ON = "SP_KEEP_SCREEN_ON"
    private const val SP_LINEAR_LAYOUT = "SP_LINEAR_LAYOUT"
    private const val SP_SCROLL_SHORTCUT = "SP_SCROLL_SHORTCUT"

    var lockLandscape: Boolean
        get() = SPStaticUtils.getBoolean(SP_LOCK_LANDSCAPE, false)
        set(value) = SPStaticUtils.put(SP_LOCK_LANDSCAPE, value)

    var maxRecentCount: String
        get() = SPStaticUtils.getString(SP_MAX_RECENT_COUNT, "9")
        set(value) = SPStaticUtils.put(SP_MAX_RECENT_COUNT, value)

    var swipeHorizontal: Boolean
        get() = SPStaticUtils.getBoolean(SP_SWIPE_HORIZONTAL, true)
        set(value) = SPStaticUtils.put(SP_SWIPE_HORIZONTAL, value)

    var nightMode: Boolean
        get() = SPStaticUtils.getBoolean(SP_NIGHT_MODE, false)
        set(value) = SPStaticUtils.put(SP_NIGHT_MODE, value)

    var volumeControl: Boolean
        get() = SPStaticUtils.getBoolean(SP_VOLUME_CONTROL, true)
        set(value) = SPStaticUtils.put(SP_VOLUME_CONTROL, value)

    var showStatusBar: Boolean
        get() = SPStaticUtils.getBoolean(SP_SHOW_STATUS_BAR, false)
        set(value) = SPStaticUtils.put(SP_SHOW_STATUS_BAR, value)

    var scrollLevel: Long
        get() = SPStaticUtils.getLong(SP_SCROLL_LEVEL, 8L)
        set(value) = SPStaticUtils.put(SP_SCROLL_LEVEL, value)

    var clickFlipPage: Boolean
        get() = SPStaticUtils.getBoolean(SP_CLICK_FLIP_PAGE, true)
        set(value) = SPStaticUtils.put(SP_CLICK_FLIP_PAGE, value)

    var keepScreenOn: Boolean
        get() = SPStaticUtils.getBoolean(SP_KEEP_SCREEN_ON, false)
        set(value) = SPStaticUtils.put(SP_KEEP_SCREEN_ON, value)

    var linearLayout: Boolean
        get() = SPStaticUtils.getBoolean(SP_LINEAR_LAYOUT, false)
        set(value) = SPStaticUtils.put(SP_LINEAR_LAYOUT, value)

    var scrollShortCut: Boolean
        get() = SPStaticUtils.getBoolean(SP_SCROLL_SHORTCUT, false)
        set(value) = SPStaticUtils.put(SP_SCROLL_SHORTCUT, value)
}