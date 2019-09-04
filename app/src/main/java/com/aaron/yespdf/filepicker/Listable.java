package com.aaron.yespdf.filepicker;

import java.io.File;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public interface Listable {

    List<File> listFile(String path);
}
