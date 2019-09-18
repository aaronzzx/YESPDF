package com.aaron.yespdf.filepicker;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface DeprecateIActivityInterface {

    void onDirTap(String path);

    void onSelectResult(List<String> pathList, int total, boolean isAllDisable);
}
