package com.aaron.yespdf.main

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface IOperation {
    fun delete(deleteLocal: Boolean)
    fun selectAll(selectAll: Boolean)
    fun cancelSelect()
    fun deleteDescription(): String?
    fun localDeleteVisibility(): Int
}