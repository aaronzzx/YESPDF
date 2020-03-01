package com.aaron.yespdf.common;

import com.blankj.utilcode.util.SPStaticUtils;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class Settings {

    private static final String SP_LOCK_LANDSCAPE = "SP_LOCK_LANDSCAPE";
    private static final String SP_MAX_RECENT_COUNT = "SP_MAX_RECENT_COUNT";
    private static final String SP_SWIPE_HORIZONTAL = "SP_SWIPE_HORIZONTAL";
    private static final String SP_NIGHT_MODE = "SP_NIGHT_MODE";
    private static final String SP_VOLUME_CONTROL = "SP_VOLUME_CONTROL";
    private static final String SP_SHOW_STATUS_BAR = "SP_SHOW_STATUS_BAR";
    private static final String SP_SCROLL_LEVEL = "SP_SCROLL_LEVEL";
    private static final String SP_CLICK_FLIP_PAGE = "SP_CLICK_FLIP_PAGE";
    private static final String SP_KEEP_SCREEN_ON = "SP_KEEP_SCREEN_ON";
    private static final String SP_DRAW_BOOKMARK = "SP_DRAW_BOOKMARK";

    private static boolean lockLandscape;
    private static String maxRecentCount;
    private static boolean swipeHorizontal;
    private static boolean nightMode;
    private static boolean volumeControl;
    private static boolean showStatusBar;
    private static long scrollLevel;
    private static boolean clickFlipPage;
    private static boolean keepScreenOn;

    static void querySettings() {
        Settings.lockLandscape = SPStaticUtils.getBoolean(SP_LOCK_LANDSCAPE, false);
        Settings.maxRecentCount = SPStaticUtils.getString(SP_MAX_RECENT_COUNT, "9");
        Settings.swipeHorizontal = SPStaticUtils.getBoolean(SP_SWIPE_HORIZONTAL, true);
        Settings.nightMode = SPStaticUtils.getBoolean(SP_NIGHT_MODE, false);
        Settings.volumeControl = SPStaticUtils.getBoolean(SP_VOLUME_CONTROL, true);
        Settings.showStatusBar = SPStaticUtils.getBoolean(SP_SHOW_STATUS_BAR, false);
        Settings.scrollLevel = SPStaticUtils.getLong(SP_SCROLL_LEVEL, 8L);
        Settings.clickFlipPage = SPStaticUtils.getBoolean(SP_CLICK_FLIP_PAGE, true);
        Settings.keepScreenOn = SPStaticUtils.getBoolean(SP_KEEP_SCREEN_ON, false);
    }

    public static void setLockLandscape(boolean lockLandscape) {
        Settings.lockLandscape = lockLandscape;
        SPStaticUtils.put(SP_LOCK_LANDSCAPE, lockLandscape);
    }

    public static void setMaxRecentCount(String maxRecentCount) {
        Settings.maxRecentCount = maxRecentCount;
        SPStaticUtils.put(SP_MAX_RECENT_COUNT, maxRecentCount);
    }

    public static void setSwipeHorizontal(boolean swipeHorizontal) {
        Settings.swipeHorizontal = swipeHorizontal;
        SPStaticUtils.put(SP_SWIPE_HORIZONTAL, swipeHorizontal);
    }

    public static void setNightMode(boolean nightMode) {
        Settings.nightMode = nightMode;
        SPStaticUtils.put(SP_NIGHT_MODE, nightMode);
    }

    public static void setVolumeControl(boolean volumeControl) {
        Settings.volumeControl = volumeControl;
        SPStaticUtils.put(SP_VOLUME_CONTROL, volumeControl);
    }

    public static void setShowStatusBar(boolean showStatusBar) {
        Settings.showStatusBar = showStatusBar;
        SPStaticUtils.put(SP_SHOW_STATUS_BAR, showStatusBar);
    }

    public static void setScrollLevel(long scrollLevel) {
        Settings.scrollLevel = scrollLevel;
        SPStaticUtils.put(SP_SCROLL_LEVEL, scrollLevel);
    }

    public static void setClickFlipPage(boolean clickFlipPage) {
        Settings.clickFlipPage = clickFlipPage;
        SPStaticUtils.put(SP_CLICK_FLIP_PAGE, clickFlipPage);
    }

    public static void setKeepScreenOn(boolean keepScreenOn) {
        Settings.keepScreenOn = keepScreenOn;
        SPStaticUtils.put(SP_KEEP_SCREEN_ON, keepScreenOn);
    }

    public static boolean isLockLandscape() {
        return lockLandscape;
    }

    public static String getMaxRecentCount() {
        return maxRecentCount;
    }

    public static boolean isSwipeHorizontal() {
        return swipeHorizontal;
    }

    public static boolean isNightMode() {
        return nightMode;
    }

    public static boolean isVolumeControl() {
        return volumeControl;
    }

    public static boolean isShowStatusBar() {
        return showStatusBar;
    }

    public static long getScrollLevel() {
        return scrollLevel;
    }

    public static boolean isClickFlipPage() {
        return clickFlipPage;
    }

    public static boolean isKeepScreenOn() {
        return keepScreenOn;
    }

    private Settings() {
    }
}
