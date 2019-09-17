package com.aaron.yespdf.filepicker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class ByNameListable implements IListable {

    @Override
    public List<File> listFile(String path) {
        File file = new File(path);
        File[] files = file.listFiles(new FileFilterImpl());
        List<File> fileList = new ArrayList<>(Arrays.asList(files != null ? files : new File[0]));
        Collections.sort(fileList, (file1, file2) -> {
            if (file1.isDirectory() && !file2.isDirectory()) {
                return -1;
            } else if (!file1.isDirectory() && file2.isDirectory()) {
                return 1;
            }
            String name1 = file1.getName();
            String name2 = file2.getName();
            String namePre1 = name1.substring(0, 1);
            String namePre2 = name2.substring(0, 1);
            // number
            String numRegex = "[0-9]";
            if (namePre1.matches(numRegex) && !namePre2.matches(numRegex)) {
                return -1;
            } else if (!namePre1.matches(numRegex) && namePre2.matches(numRegex)) {
                return 1;
            } else if (namePre1.matches(numRegex) && namePre2.matches(numRegex)) {
                return name1.compareTo(name2);
            }
            // chinese
            String zhRegex = "[^a-zA-Z]+";
            if (namePre1.matches(zhRegex) && !namePre2.matches(zhRegex)) {
                return -1;
            } else if (!namePre1.matches(zhRegex) && namePre2.matches(zhRegex)) {
                return 1;
            } else if (namePre1.matches(zhRegex) && namePre2.matches(zhRegex)) {
                return name1.compareTo(name2);
            }
            return name1.toLowerCase().compareTo(name2.toLowerCase());
        });
        return fileList;
    }
}
