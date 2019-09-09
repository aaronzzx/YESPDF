package com.aaron.yespdf.common.event;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class RecentPDFEvent {

    private boolean readStateChange;

    public RecentPDFEvent() {
    }

    public RecentPDFEvent(boolean readStateChange) {
        this.readStateChange = readStateChange;
    }

    public boolean isReadStateChange() {
        return readStateChange;
    }

    public void setReadStateChange(boolean readStateChange) {
        this.readStateChange = readStateChange;
    }
}
