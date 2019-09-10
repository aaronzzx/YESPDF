package com.aaron.yespdf.common.event;

import com.shockwave.pdfium.PdfDocument;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class ContentEvent {

    private List<PdfDocument.Bookmark> bookmarkList;

    public ContentEvent(List<PdfDocument.Bookmark> bookmarkList) {
        this.bookmarkList = bookmarkList;
    }

    public List<PdfDocument.Bookmark> getBookmarkList() {
        return bookmarkList;
    }
}
