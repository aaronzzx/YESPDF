package com.aaron.yespdf.preview

import com.shockwave.pdfium.PdfDocument

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface IContentFragInterface {
    fun update(collection: MutableCollection<PdfDocument.Bookmark>)
}