package com.aaron.yespdf.filepicker

import java.io.File

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface IListable {
    fun listFile(path: String?): List<File>
}