package com.aaron.yespdf.common.bean;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class Cover {

    public String name;
    public List<String> coverList;
    public int count;

    public Cover(String name, List<String> coverList, int count) {
        this.name = name;
        this.coverList = coverList;
        this.count = count;
    }

    @Override
    public String toString() {
        return "Cover{" +
                "name='" + name + '\'' +
                ", coverList=" + coverList +
                ", count=" + count +
                '}';
    }
}
