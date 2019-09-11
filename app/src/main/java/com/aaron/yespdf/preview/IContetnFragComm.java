package com.aaron.yespdf.preview;

import com.shockwave.pdfium.PdfDocument;

import java.util.Collection;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface IContetnFragComm {

    void update(Collection<PdfDocument.Bookmark> collection);
}
