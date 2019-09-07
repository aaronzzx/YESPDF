package com.aaron.yespdf.common.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
@Entity
public class RecentPDF implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.dir);
        dest.writeString(this.name);
    }

    protected RecentPDF(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.dir = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<RecentPDF> CREATOR = new Parcelable.Creator<RecentPDF>() {
        @Override
        public RecentPDF createFromParcel(Parcel source) {
            return new RecentPDF(source);
        }

        @Override
        public RecentPDF[] newArray(int size) {
            return new RecentPDF[size];
        }
    };
}
