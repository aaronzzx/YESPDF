package com.aaron.yespdf.common.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
@Entity
public class PDF {

    @Id(autoincrement = true) Long id;
    @Unique String path;
    String dir;
    String name;
    String cover;
    String progress;
    int curPage;
    int totalPage;
    int bookmarkPage;
    long latestRead;

    public PDF() {

    }

    public PDF(String path, String dir, String name, String cover, String progress, int curPage, int totalPage, int bookmarkPage, long latestRead) {
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

    @Generated(hash = 1105056540)
    public PDF(Long id, String path, String dir, String name, String cover, String progress, int curPage, int totalPage, int bookmarkPage,
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

    public int getBookmarkPage() {
        return bookmarkPage;
    }

    public void setBookmarkPage(int bookmarkPage) {
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
}
