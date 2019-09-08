package com.aaron.yespdf.common.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
@Entity
public class PDF implements Parcelable {

    @Id(autoincrement = true) Long id;
    @Unique String path;
    String dir;
    String name;
    String cover;
    String progress;
    int curPage;
    int totalPage;
    String bookmarkPage;
    long latestRead;

    public PDF() {

    }

    @Generated(hash = 549187711)
    public PDF(Long id, String path, String dir, String name, String cover, String progress, int curPage, int totalPage, String bookmarkPage,
            long latestRead) {
        this.id = id;
        this.path = path;
        this.dir = dir;
        this.name = name;
        this.cover = cover;
        this.progress = progress;
        this.curPage = curPage;
        this.totalPage = totalPage;
        this.bookmarkPage = bookmarkPage;
        this.latestRead = latestRead;
    }

    @Override
    public String toString() {
        return "PDF{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", dir='" + dir + '\'' +
                ", name='" + name + '\'' +
                ", cover='" + cover + '\'' +
                ", progress='" + progress + '\'' +
                ", curPage=" + curPage +
                ", totalPage=" + totalPage +
                ", bookmarkPage=" + bookmarkPage +
                ", latestRead=" + latestRead +
                '}';
    }

    public Long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public String getBookmarkPage() {
        return bookmarkPage;
    }

    public void setBookmarkPage(String bookmarkPage) {
        this.bookmarkPage = bookmarkPage;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getLatestRead() {
        return latestRead;
    }

    public void setLatestRead(long latestRead) {
        this.latestRead = latestRead;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.path);
        dest.writeString(this.dir);
        dest.writeString(this.name);
        dest.writeString(this.cover);
        dest.writeString(this.progress);
        dest.writeInt(this.curPage);
        dest.writeInt(this.totalPage);
        dest.writeString(this.bookmarkPage);
        dest.writeLong(this.latestRead);
    }

    protected PDF(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.path = in.readString();
        this.dir = in.readString();
        this.name = in.readString();
        this.cover = in.readString();
        this.progress = in.readString();
        this.curPage = in.readInt();
        this.totalPage = in.readInt();
        this.bookmarkPage = in.readString();
        this.latestRead = in.readLong();
    }

    public static final Parcelable.Creator<PDF> CREATOR = new Parcelable.Creator<PDF>() {
        @Override
        public PDF createFromParcel(Parcel source) {
            return new PDF(source);
        }

        @Override
        public PDF[] newArray(int size) {
            return new PDF[size];
        }
    };
}
