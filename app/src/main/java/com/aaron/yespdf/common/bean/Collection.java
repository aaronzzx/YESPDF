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
public class Collection implements Parcelable {

    @Id(autoincrement = true)
    Long id;
    @Unique
    String name;

    public Collection() {

    }

    public Collection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    protected Collection(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.name = in.readString();
    }

    @Generated(hash = 721076930)
    public Collection(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static final Parcelable.Creator<Collection> CREATOR = new Parcelable.Creator<Collection>() {
        @Override
        public Collection createFromParcel(Parcel source) {
            return new Collection(source);
        }

        @Override
        public Collection[] newArray(int size) {
            return new Collection[size];
        }
    };
}
