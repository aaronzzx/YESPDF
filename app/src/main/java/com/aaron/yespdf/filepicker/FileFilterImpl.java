package com.aaron.yespdf.filepicker;

import java.io.File;
import java.io.FileFilter;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class FileFilterImpl implements FileFilter {

    @Override
    public boolean accept(File file) {
        return !file.getName().startsWith(".") && (file.isDirectory() || file.getName().endsWith(".pdf"));
    }
}
