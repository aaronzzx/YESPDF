package com.aaron.yespdf.filepicker;

import android.view.View;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface IActivityInterface {

    void onDirTap(String dirPath);

    void onSelectResult(List<String> pathList, int total);

    View getViewSelectAll();
}
