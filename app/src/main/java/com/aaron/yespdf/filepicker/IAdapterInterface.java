package com.aaron.yespdf.filepicker;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface IAdapterInterface {

    void reset();

    void selectAll(boolean flag);

    List<String> selectResult();
}
