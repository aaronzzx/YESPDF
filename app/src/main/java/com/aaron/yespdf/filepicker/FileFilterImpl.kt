package com.aaron.yespdf.filepicker

import java.io.File
import java.io.FileFilter

/**
 * @author Aaron aaronzzxup@gmail.com
 */
internal class FileFilterImpl : FileFilter {
    override fun accept(file: File): Boolean {
        return !file.name.startsWith(".") && (file.isDirectory || file.name.endsWith(".pdf"))
    }
}