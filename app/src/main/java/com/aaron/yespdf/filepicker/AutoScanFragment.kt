package com.aaron.yespdf.filepicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aaron.base.impl.OnClickListenerImpl
import com.aaron.yespdf.R
import com.aaron.yespdf.common.CommonFragment
import kotlinx.android.synthetic.main.app_fragment_auto_scan.*
import java.util.*

class AutoScanFragment : CommonFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.app_fragment_auto_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
        (activity as SelectActivity).ibtnSelectAll.visibility = View.GONE
        (activity as SelectActivity).ibtnSearch.visibility = View.GONE
        (activity as SelectActivity).closeSearchView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            activity.finish()
        }
    }

    private fun initView() {
        app_btn_scan.setOnClickListener(object : OnClickListenerImpl() {
            override fun onViewClick(v: View, interval: Long) {
                ScanActivity.start(activity, (activity as SelectActivity).importeds as ArrayList<String?>, SelectActivity.REQUEST_CODE)
            }
        })
    }

    companion object {
        private const val REQUEST_CODE = 1
        fun newInstance(): Fragment {
            return AutoScanFragment()
        }
    }
}