package com.aaron.yespdf.common.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class NewViewPager extends ViewPager {

    private boolean mScrollable = true;

    public NewViewPager(@NonNull Context context) {
        super(context);
    }

    public NewViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mScrollable) {
            return super.onTouchEvent(ev);
        }
        return false; // 必须返回 false ，否则子 View 无法处理触摸事件
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mScrollable) {
            return super.onInterceptTouchEvent(ev);
        }
        return false; // 必须返回 false ，否则子 View 无法处理触摸事件
    }

    /**
     * 是否允许左右滑动
     */
    public void setScrollable(boolean enable) {
        mScrollable = enable;
    }
}
