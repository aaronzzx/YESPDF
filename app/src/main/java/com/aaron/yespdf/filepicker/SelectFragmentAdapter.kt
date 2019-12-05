package com.aaron.yespdf.filepicker

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.aaron.yespdf.R
import com.aaron.yespdf.common.App

/**
 * @author Aaron aaronzzxup@gmail.com
 */
internal class SelectFragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            AutoScanFragment.newInstance()
        } else ViewAllFragment.newInstance()
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return TITLES[position]
    }

    companion object {
        private val TITLES = arrayOf(App.getContext().getString(R.string.app_auto_scan), App.getContext().getString(R.string.app_view_all))
    }
}