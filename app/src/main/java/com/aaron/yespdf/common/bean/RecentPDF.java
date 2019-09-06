package com.aaron.yespdf.common.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
@Entity
public class RecentPDF {

    @Id(autoincrement = true) Long id;
    String dir;
    @Unique String name;

    public RecentPDF() {

    }

    public RecentPDF(String dir, String name) {
        this.dir = dir;
        this.name = name;
    }

    @Generated(hash = 705905111)
    public RecentPDF(Long id, String dir, String name) {
        this.id = id;
        this.dir = dir;
        this.name = name;
    }

    @Override
    public String toString() {
        return "RecentPDF{" +
                "id=" + id +
                ", dir='" + dir + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public Long getId() {
        return id;
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

    public void setId(Long id) {
        this.id = id;
    }
}
