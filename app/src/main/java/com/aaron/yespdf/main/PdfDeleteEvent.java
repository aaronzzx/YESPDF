package com.aaron.yespdf.main;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class PdfDeleteEvent {

    List<String> deleted;
    String dir;
    boolean isEmpty;

    PdfDeleteEvent(List<String> deleted, String dir, boolean isEmpty) {
        this.deleted = deleted;
        this.dir = dir;
        this.isEmpty = isEmpty;
    }
}
