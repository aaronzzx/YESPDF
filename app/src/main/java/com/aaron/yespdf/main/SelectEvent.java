package com.aaron.yespdf.main;

import com.aaron.yespdf.common.bean.PDF;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class SelectEvent {

    List<PDF> pdfList;
    boolean isSelectAll;

    SelectEvent(List<PDF> pdfList, boolean isSelectAll) {
        this.pdfList = pdfList;
        this.isSelectAll = isSelectAll;
    }

    @Override
    public String toString() {
        return "SelectEvent{" +
                "pdfList=" + pdfList +
                ", isSelectAll=" + isSelectAll +
                '}';
    }
}
