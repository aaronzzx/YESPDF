package com.aaron.yespdf.common.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
@Entity
public class Collection {

    @Id(autoincrement = true) Long id;
    String cover1;
    String cover2;
    String cover3;
    String cover4;
    @Unique String name;
    int count;

    public Collection() {

    }

    public Collection(String cover1, String cover2, String cover3, String cover4, String name, int count) {
        this.cover1 = cover1;
        this.cover2 = cover2;
        this.cover3 = cover3;
        this.cover4 = cover4;
        this.name = name;
        this.count = count;
    }

    @Generated(hash = 1706173321)
    public Collection(Long id, String cover1, String cover2, String cover3, String cover4, String name,
            int count) {
        this.id = id;
        this.cover1 = cover1;
        this.cover2 = cover2;
        this.cover3 = cover3;
        this.cover4 = cover4;
        this.name = name;
        this.count = count;
    }

    @Override
    public String toString() {
        return "Collection{" +
                "id=" + id +
                ", cover1='" + cover1 + '\'' +
                ", cover2='" + cover2 + '\'' +
                ", cover3='" + cover3 + '\'' +
                ", cover4='" + cover4 + '\'' +
                ", name='" + name + '\'' +
                ", count=" + count +
                '}';
    }

    public Long getId() {
        return id;
    }

    public String getCover1() {
        return cover1;
    }

    public void setCover1(String cover1) {
        this.cover1 = cover1;
    }

    public String getCover2() {
        return cover2;
    }

    public void setCover2(String cover2) {
        this.cover2 = cover2;
    }

    public String getCover3() {
        return cover3;
    }

    public void setCover3(String cover3) {
        this.cover3 = cover3;
    }

    public String getCover4() {
        return cover4;
    }

    public void setCover4(String cover4) {
        this.cover4 = cover4;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
