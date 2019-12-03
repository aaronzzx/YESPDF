package com.aaron.yespdf.filepicker

import java.io.File
import java.io.FileFilter
import java.util.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
internal class ByNameListable : IListable {

    private var fileFilter: FileFilter

    constructor() {
        fileFilter = FileFilterImpl()
    }

    constructor(fileFilter: FileFilter) {
        this.fileFilter = fileFilter
    }

    override fun listFile(path: String?): List<File> {
        val file = File(path)
        val files = file.listFiles(fileFilter)
        val fileList: List<File> = ArrayList(Arrays.asList(*files ?: arrayOfNulls(0)))
        Collections.sort(fileList) { file1: File, file2: File ->
            if (file1.isDirectory && !file2.isDirectory) {
                return@sort -1
            } else if (!file1.isDirectory && file2.isDirectory) {
                return@sort 1
            }
            val name1 = file1.name
            val name2 = file2.name
            val namePre1 = name1.substring(0, 1)
            val namePre2 = name2.substring(0, 1)
            // number
            val numRegex = "[0-9]".toRegex()
            if (namePre1.matches(numRegex) && !namePre2.matches(numRegex)) {
                return@sort -1
            } else if (!namePre1.matches(numRegex) && namePre2.matches(numRegex)) {
                return@sort 1
            } else if (namePre1.matches(numRegex) && namePre2.matches(numRegex)) {
                return@sort name1.compareTo(name2)
            }
            // chinese
            val zhRegex = "[^a-zA-Z]+".toRegex()
            if (namePre1.matches(zhRegex) && !namePre2.matches(zhRegex)) {
                return@sort -1
            } else if (!namePre1.matches(zhRegex) && namePre2.matches(zhRegex)) {
                return@sort 1
            } else if (namePre1.matches(zhRegex) && namePre2.matches(zhRegex)) {
                return@sort name1.compareTo(name2)
            }
            name1.toLowerCase().compareTo(name2.toLowerCase())
        }
        return fileList
    }
}