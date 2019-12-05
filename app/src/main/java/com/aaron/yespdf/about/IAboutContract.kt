package com.aaron.yespdf.about

import com.aaron.yespdf.common.IPresenter
import com.aaron.yespdf.common.IView

/**
 * @author Aaron aaronzheng9603@gmail.com
 */
interface IAboutView : IView {
    fun onShowMessage(list: List<Message>)
    fun onShowLibrary(list: List<Library>)
}

abstract class IAboutPresenter(view: IAboutView) : IPresenter<IAboutView>(view) {
    abstract fun requestMessage(iconId: IntArray, title: Array<String>)
    abstract fun requestLibrary(name: Array<String>, author: Array<String>, introduce: Array<String>)
}