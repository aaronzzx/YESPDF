package com.aaron.yespdf.filepicker

import androidx.annotation.StringRes
import com.aaron.yespdf.common.IModel
import com.aaron.yespdf.common.IPresenter
import com.aaron.yespdf.common.IView
import com.blankj.utilcode.util.SDCardUtils.SDCardInfo
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface IViewAllModel : IModel {
    fun listStorage(callback: (List<SDCardInfo>?) -> Unit)
    fun listFile(path: String?, callback: (List<File>?) -> Unit)
    fun saveLastPath(path: String?)
    fun queryLastPath(): String?

    companion object {
        const val SP_LAST_PATH = "SP_LAST_PATH" // 使用首选项存放退出之前的路径
    }
}

interface IViewAllView : IView {
    fun attachP()
    fun onShowMessage(@StringRes stringId: Int)
    fun onShowFileList(fileList: List<File>?)
    fun onShowPath(pathList: List<String>)
}

abstract class IViewAllPresenter(view: IViewAllView) : IPresenter<IViewAllView>(view) {
    protected abstract val model: IViewAllModel
    protected var fileList: MutableList<File> = ArrayList()

    abstract fun canFinish(): Boolean
    abstract fun goBack()
    abstract fun listStorage()
    abstract fun listFile(path: String?)
    abstract fun detach()

    companion object {
        const val ROOT_PATH = "/storage/emulated"
    }
}