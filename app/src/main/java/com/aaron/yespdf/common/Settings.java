package com.aaron.yespdf.common;

import com.blankj.utilcode.util.SPStaticUtils;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class Settings {

    private static final String SP_SWIPE_HORIZONTAL = "SP_SWIPE_HORIZONTAL";
    private static final String SP_NIGHT_MODE       = "SP_NIGHT_MODE";
    private static final String SP_VOLUME_CONTROL   = "SP_VOLUME_CONTROL";
    private static final String SP_SCROLL_LEVEL = "SP_SCROLL_LEVEL";

    private static boolean swipeHorizontal;
    private static boolean nightMode;
    private static boolean volumeControl;
    private static long scrollLevel;

    static void querySettings() {
        Settings.swipeHorizontal = SPStaticUtils.getBoolean(SP_SWIPE_HORIZONTAL, true);
        Settings.nightMode       = SPStaticUtils.getBoolean(SP_NIGHT_MODE, false);
        Settings.volumeControl   = SPStaticUtils.getBoolean(SP_VOLUME_CONTROL, true);
        Settings.scrollLevel = SPStaticUtils.getLong(SP_SCROLL_LEVEL, 5L);
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

    public static void setScrollLevel(long scrollLevel) {
        Settings.scrollLevel = scrollLevel;
        SPStaticUtils.put(SP_SCROLL_LEVEL, scrollLevel);
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

    public static long getScrollLevel() {
        return scrollLevel;
    }

    private Settings() {}
}
