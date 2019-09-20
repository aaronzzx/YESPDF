package com.aaron.yespdf.main;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface IActivityInterface {

    void startOperation();

    void onTap(String name);

    <T> void onSelect(List<T> list, boolean isSelectAll);
}
