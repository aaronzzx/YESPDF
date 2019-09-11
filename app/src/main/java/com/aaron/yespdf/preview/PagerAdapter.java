package com.aaron.yespdf.preview;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.shockwave.pdfium.PdfDocument;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class PagerAdapter extends FragmentPagerAdapter {

    @SuppressLint("WrongConstant")
    PagerAdapter(@NonNull FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return ContentFragment.newInstance();
        }
        return BookmarkFragment.newInstance();
    }

    @Override
    public int getCount() {
        return 2;
    }
}
