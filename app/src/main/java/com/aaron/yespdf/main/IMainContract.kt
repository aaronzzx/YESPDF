package com.aaron.yespdf.main

import android.content.Intent
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
    abstract fun insertPDF(data: Intent?)
}