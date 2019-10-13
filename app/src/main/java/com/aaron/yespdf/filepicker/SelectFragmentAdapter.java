package com.aaron.yespdf.filepicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.App;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class SelectFragmentAdapter extends FragmentPagerAdapter {

    private static final String[] TITLES = {App.getContext().getString(R.string.app_auto_scan), App.getContext().getString(R.string.app_view_all)};

    SelectFragmentAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return AutoScanFragment.newInstance();
        }
        return ViewAllFragment.newInstance();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }
}
