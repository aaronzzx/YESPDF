package com.aaron.yespdf.filepicker

import com.aaron.yespdf.filepicker.IViewAllModel.Companion.SP_LAST_PATH
import com.blankj.utilcode.util.SDCardUtils
import com.blankj.utilcode.util.SDCardUtils.SDCardInfo
import com.blankj.utilcode.util.SPStaticUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ThreadUtils.SimpleTask
import java.io.File

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class ViewAllModel : IViewAllModel {

    private val mListable: IListable

    override fun listStorage(callback: (List<SDCardInfo>?) -> Unit) {
        ThreadUtils.executeByIo<List<SDCardInfo>>(object : SimpleTask<List<SDCardInfo>>() {
            override fun doInBackground(): List<SDCardInfo> {
                return SDCardUtils.getSDCardInfo()
            }

            override fun onSuccess(result: List<SDCardInfo>?) {
                callback(result)
            }
        })
    }

    override fun listFile(path: String?, callback: (List<File>?) -> Unit) {
        ThreadUtils.executeByIo<List<File>>(object : SimpleTask<List<File>?>() {
            override fun doInBackground(): List<File>? {
                return mListable.listFile(path)
            }

            override fun onSuccess(result: List<File>?) {
                callback(result)
            }
        })
    }

    override fun saveLastPath(path: String?) {
        SPStaticUtils.put(SP_LAST_PATH, path)
    }

    override fun queryLastPath(): String? {
        return SPStaticUtils.getString(SP_LAST_PATH, "")
    }

    init {
        mListable = ByNameListable()
    }
}