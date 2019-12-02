package com.aaron.yespdf.main

import android.content.Intent
import com.aaron.yespdf.common.DBHelper
import com.aaron.yespdf.common.DataManager
import com.aaron.yespdf.filepicker.SelectActivity
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ThreadUtils.SimpleTask

/**
 * @author Aaron aaronzzxup@gmail.com
 */
internal class MainPresenter(view: IMainView) : IMainPresenter(view) {

    override fun insertPDF(data: Intent?) {
        view.onShowLoading()
        if (data != null) {
            val pathList: List<String> = data.getStringArrayListExtra(SelectActivity.EXTRA_SELECTED)
            val type = data.getIntExtra(SelectActivity.EXTRA_TYPE, 0)
            val groupName = data.getStringExtra(SelectActivity.EXTRA_GROUP_NAME)
            insertPDF(pathList, type, groupName)
        }
    }

    private fun insertPDF(pathList: List<String>, type: Int, groupName: String) {
        ThreadUtils.executeByCached<Boolean>(object : SimpleTask<Boolean?>() {
            override fun doInBackground(): Boolean {
                when (type) {
                    SelectActivity.TYPE_BASE_FOLDER -> DBHelper.insert(pathList)
                    SelectActivity.TYPE_TO_EXIST, SelectActivity.TYPE_CUSTOM -> DBHelper.insert(pathList, groupName)
                }
                return false
            }

            override fun onSuccess(success: Boolean?) {
                DataManager.updateAll()
                view.onUpdate()
                view.onHideLoading()
            }
        })
    }
}