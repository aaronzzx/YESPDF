package com.aaron.yespdf.main

import com.aaron.yespdf.common.DBHelper
import com.aaron.yespdf.common.DataManager
import com.aaron.yespdf.filepicker.SelectActivity
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ThreadUtils.SimpleTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * @author Aaron aaronzzxup@gmail.com
 */
internal class MainPresenter(view: IMainView) : IMainPresenter(view) {

    override fun insertPDF(paths: List<String>?, type: Int?, groupName: String?) {
        if (paths == null || type == null) {
            return
        }
        (view as CoroutineScope).launch {
            view.onShowLoading()
            withContext(Dispatchers.IO) {
                if (type == SelectActivity.TYPE_BASE_FOLDER) {
                    DBHelper.insert(paths)
                } else { // SelectActivity.TYPE_TO_EXIST, SelectActivity.TYPE_CUSTOM
                    DBHelper.insert(paths, groupName ?: "BASE_FOLDER")
                }
            }
            DataManager.updateAll()
            view.onUpdate()
            view.onHideLoading()
        }
    }
}