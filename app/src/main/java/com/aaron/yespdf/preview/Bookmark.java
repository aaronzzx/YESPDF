package com.aaron.yespdf.preview;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class Bookmark implements Parcelable {

    /**
     * {"pageId":123,"title":"如何使用 ContentProvider","time":1234123412}
     */
    private Integer pageId;
    private String title;
    private long time;

    public Bookmark() {
    }

    public Bookmark(Integer pageId, String title, long time) {
        this.pageId = pageId;
        this.title = title;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Bookmark{" +
                "pageId=" + pageId +
                ", title='" + title + '\'' +
                ", time=" + time +
                '}';
    }

    public Integer getPageId() {
        return pageId;
    }

    public void setPageId(Integer pageId) {
        this.pageId = pageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.pageId);
        dest.writeString(this.title);
        dest.writeLong(this.time);
    }

    protected Bookmark(Parcel in) {
        this.pageId = (Integer) in.readValue(Integer.class.getClassLoader());
        this.title = in.readString();
        this.time = in.readLong();
    }

    public static final Parcelable.Creator<Bookmark> CREATOR = new Parcelable.Creator<Bookmark>() {
        @Override
        public Bookmark createFromParcel(Parcel source) {
            return new Bookmark(source);
        }

        @Override
        public Bookmark[] newArray(int size) {
            return new Bookmark[size];
        }
    };
}
