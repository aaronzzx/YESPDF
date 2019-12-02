package com.aaron.yespdf.common

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface IModel

interface IView

abstract class IPresenter<V: IView>(protected var view: V)