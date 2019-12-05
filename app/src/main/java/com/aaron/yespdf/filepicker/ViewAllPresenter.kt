package com.aaron.yespdf.filepicker

import com.blankj.utilcode.util.StringUtils
import java.io.File
import java.util.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class ViewAllPresenter(view: IViewAllView) : IViewAllPresenter(view) {

    private var curPath: String? = ROOT_PATH
    private var isFirstIn = true

    override val model: IViewAllModel = ViewAllModel()

    override fun detach() {
        model.saveLastPath(curPath)
    }

    override fun canFinish(): Boolean {
        return curPath == ROOT_PATH
    }

    override fun goBack() {
        val prePath = curPath?.substring(0, curPath?.lastIndexOf("/") ?: 0)
        listFile(prePath)
    }

    override fun listStorage() {
        if (isFirstIn) {
            isFirstIn = false
            val lastPath = model.queryLastPath()
            if (StringUtils.isEmpty(lastPath) || ROOT_PATH == lastPath) {
                listStorage()
            } else {
                listFile(lastPath)
            }
            return
        }
        model.listStorage { result ->
            fileList.clear()
            if (result != null) {
                for (info in result) {
                    if (info.state == "mounted") {
                        fileList.add(File(info.path))
                    }
                }
            }
            view.onShowFileList(fileList)
        }
    }

    override fun listFile(path: String?) {
        curPath = path
        if (path == ROOT_PATH) {
            listStorage()
            return
        }
        model.listFile(path) { result ->
            path?.run {
                if (length > 18) {
                    val path1 = substring(18) // 去除 /storage/emulated/
                    val paths = listOf(*path1.split("/").toTypedArray())
                    view.onShowPath(paths)
                }
            }
            view.onShowFileList(result)
        }
    }
}