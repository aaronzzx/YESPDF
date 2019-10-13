package com.aaron.yespdf.common;

import com.aaron.yespdf.common.bean.Collection;
import com.aaron.yespdf.common.bean.Cover;
import com.aaron.yespdf.common.bean.PDF;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class DataManager {

    private static List<String> pathList;
    private static List<PDF> pdfList;
    private static List<PDF> recentPdfList;
    private static List<Collection> collectionList;

    private static List<Cover> coverList = new ArrayList<>();
    private static List<PDF> tempList = new ArrayList<>();

    static void init() {
        pdfList = DBHelper.queryAllPDF();
        recentPdfList = DBHelper.queryRecentPDF();
        collectionList = DBHelper.queryAllCollection();
        updatePathList();
        updateCoverList();
    }

    public static void updateAll() {
        DataManager.collectionList.clear();
        DataManager.collectionList.addAll(DBHelper.queryAllCollection());
        DataManager.recentPdfList.clear();
        DataManager.recentPdfList.addAll(DBHelper.queryRecentPDF());
        DataManager.pdfList.clear();
        DataManager.pdfList.addAll(DBHelper.queryAllPDF());
        updatePathList();
        updateCoverList();
    }

    public static void updatePDFs() {
        DataManager.pdfList.clear();
        DataManager.pdfList.addAll(DBHelper.queryAllPDF());
        updatePathList();
        updateCoverList();
    }

    public static void updateRecentPDFs() {
        DataManager.recentPdfList.clear();
        DataManager.recentPdfList.addAll(DBHelper.queryRecentPDF());
    }

    public static void updateCollection() {
        DataManager.collectionList.clear();
        DataManager.collectionList.addAll(DBHelper.queryAllCollection());
        updateCoverList();
    }

    private static void updateCoverList() {
        DataManager.coverList.clear();
        for (Collection c : DataManager.collectionList) {
            List<PDF> temp = getPdfList(c.getName());
            List<PDF> list;
            switch (temp.size()) {
                case 0:
                    continue;
                case 1:
                    list = temp.subList(0, 1);
                    break;
                case 2:
                    list = temp.subList(0, 2);
                    break;
                case 3:
                    list = temp.subList(0, 3);
                    break;
                default:
                    list = temp.subList(0, 4);
                    break;
            }
            String name = temp.get(0).getDir();
            List<String> coverList = new ArrayList<>();
            for (PDF pdf : list) {
                coverList.add(pdf.getCover());
            }
            int count = temp.size();
            Cover cover = new Cover(name, coverList, count);
            DataManager.coverList.add(cover);
        }
    }

    private static void updatePathList() {
        if (pathList == null) {
            pathList = new ArrayList<>();
        }
        pathList.clear();
        for (PDF pdf : pdfList) {
            pathList.add(pdf.getPath());
        }
    }

    public static List<PDF> getRecentPdfList() {
        return recentPdfList;
    }

    public static List<String> getPathList() {
        return pathList;
    }

    public static List<PDF> getPdfList() {
        return pdfList;
    }

    public static List<PDF> getPdfList(String name) {
        DataManager.tempList.clear();
        for (PDF pdf : DataManager.pdfList) {
            if (pdf.getDir().equals(name)) {
                tempList.add(pdf);
            }
        }
        return DataManager.tempList;
    }

    public static List<Collection> getCollectionList() {
        return collectionList;
    }

    public static List<Cover> getCoverList() {
        return coverList;
    }

    private DataManager() {}
}
