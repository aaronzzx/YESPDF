package com.aaron.yespdf.filepicker;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface ITestAdapterInterface {

    void reset();

    void selectAll(boolean flag);

    List<String> selectResult();
}
