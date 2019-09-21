package com.aaron.yespdf.main;

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
class MainFragmentAdapter extends FragmentPagerAdapter {

    private static final String[] TITLES = {App.getContext().getString(R.string.app_recent), App.getContext().getString(R.string.app_all)};

    MainFragmentAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return RecentFragment.newInstance();
        }
        return AllFragment.newInstance();
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
