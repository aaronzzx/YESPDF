package com.aaron.yespdf.preview

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * @author Aaron aaronzzxup@gmail.com
 */
internal class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_SET_USER_VISIBLE_HINT) {

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            ContentFragment.newInstance()
        } else BookmarkFragment.newInstance()
    }

    override fun getCount(): Int {
        return 2
    }
}