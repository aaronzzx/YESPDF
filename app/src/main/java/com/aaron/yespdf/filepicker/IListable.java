package com.aaron.yespdf.filepicker;

import java.io.File;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public interface IListable {

    List<File> listFile(String path);
}
