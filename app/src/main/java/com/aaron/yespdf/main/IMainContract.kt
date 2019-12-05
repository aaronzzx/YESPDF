package com.aaron.yespdf.main

import androidx.annotation.StringRes
import com.aaron.yespdf.common.IPresenter
import com.aaron.yespdf.common.IView

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface IMainView : IView {
    fun onShowMessage(@StringRes stringId: Int)
    fun onShowLoading()
    fun onHideLoading()
    fun onUpdate()
}

abstract class IMainPresenter(view: IMainView) : IPresenter<IMainView>(view) {
    abstract fun insertPDF(paths: List<String>?, type: Int?, groupName: String?)
}