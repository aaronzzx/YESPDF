package com.aaron.yespdf.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.aaron.yespdf.R
import com.aaron.yespdf.common.App

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class MainFragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            RecentFragment.newInstance()
        } else AllFragment2.newInstance()
    }

    override fun getCount(): Int = 2

    override fun getPageTitle(position: Int): CharSequence? = TITLES[position]

    companion object {
        private val TITLES = arrayOf(App.getContext().getString(R.string.app_recent), App.getContext().getString(R.string.app_all))
    }
}