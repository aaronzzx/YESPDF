package com.aaron.yespdf.common.event;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class RecentPDFEvent {

    private boolean fromPreviewActivity;

    public RecentPDFEvent() {
    }

    public RecentPDFEvent(boolean fromPreviewActivity) {
        this.fromPreviewActivity = fromPreviewActivity;
    }

    public boolean isFromPreviewActivity() {
        return fromPreviewActivity;
    }

    public void setFromPreviewActivity(boolean fromPreviewActivity) {
        this.fromPreviewActivity = fromPreviewActivity;
    }
}
