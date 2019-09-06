package com.aaron.yespdf.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.aaron.yespdf.common.bean.Collection;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.greendao.DaoMaster;
import com.aaron.yespdf.common.greendao.DaoSession;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class DBManager {

    private static DaoSession sDaoSession;

    public static void init(Context context, String dbName) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, dbName);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        sDaoSession = daoMaster.newSession();
    }

    public static List<Collection> queryAllCollection() {
        return sDaoSession.loadAll(Collection.class);
    }

    public static List<PDF> queryAllPDF() {
        return sDaoSession.loadAll(PDF.class);
    }

    public static List<PDF> queryPDF(String name) {
        return sDaoSession.queryRaw(PDF.class, "where DIR = ?", name);
    }

    public static void insert(List<String> pathList) throws IOException {
        // 实际文件路径与文件名称
        String actualPath1 = pathList.get(0);
        String actualPath2 = pathList.get(1);
        String actualPath3 = pathList.get(2);
        String actualPath4 = pathList.get(3);
        String fileName1 = actualPath1.substring(actualPath1.lastIndexOf("/") + 1, actualPath1.length() - 4);
        String fileName2 = actualPath2.substring(actualPath2.lastIndexOf("/") + 1, actualPath2.length() - 4);
        String fileName3 = actualPath3.substring(actualPath3.lastIndexOf("/") + 1, actualPath3.length() - 4);
        String fileName4 = actualPath4.substring(actualPath4.lastIndexOf("/") + 1, actualPath4.length() - 4);

        // 去除了文件名称的父路径
        String parentPath = actualPath1.substring(0, pathList.get(0).lastIndexOf("/"));
        // Collection 属性
        String name = parentPath.substring(parentPath.lastIndexOf("/") + 1);
        int count = pathList.size();
        String cover1 = PathUtils.getExternalAppDataPath() + "/cache/" + fileName1 + ".jpg";
        String cover2 = PathUtils.getExternalAppDataPath() + "/cache/" + fileName2 + ".jpg";
        String cover3 = PathUtils.getExternalAppDataPath() + "/cache/" + fileName3 + ".jpg";
        String cover4 = PathUtils.getExternalAppDataPath() + "/cache/" + fileName4 + ".jpg";
        // 制作 PDF 封面并缓存
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Object>() {
            @Nullable
            @Override
            public Object doInBackground() throws Throwable {
                PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(pathList.get(0), 0), cover1);
                PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(pathList.get(1), 0), cover2);
                PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(pathList.get(2), 0), cover3);
                PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(pathList.get(3), 0), cover4);
                return null;
            }

            @Override
            public void onSuccess(@Nullable Object result) {

            }
        });

        Collection collection = new Collection();
        collection.setName(name);
        collection.setCount(count);
        collection.setCover1(cover1);
        collection.setCover2(cover2);
        collection.setCover3(cover3);
        collection.setCover4(cover4);
        sDaoSession.insertOrReplace(collection);
        insertPDFs(name, pathList);
    }

    private static void insertPDFs(String dir, List<String> pathList) throws IOException {
        for (String path : pathList) {
            int curPage = 0;
            int bookmarkPage = 0;
            int totalPage = PdfUtils.getPdfTotalPage(path);
            String name = path.substring(path.lastIndexOf("/") + 1, path.length() - 4);
            String progress = "0.0%";
            String cover = PathUtils.getExternalAppDataPath() + "/cache/" + name + ".jpg";
            // 制作 PDF 封面并缓存
            ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Object>() {
                @Nullable
                @Override
                public Object doInBackground() throws Throwable {
                    PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(path, 0), cover);
                    return null;
                }

                @Override
                public void onSuccess(@Nullable Object result) {

                }
            });

            PDF pdf = new PDF();
            pdf.setDir(dir);
            pdf.setName(name);
            pdf.setCover(cover);
            pdf.setPath(path);
            pdf.setProgress(progress);
            pdf.setCurPage(curPage);
            pdf.setBookmarkPage(bookmarkPage);
            pdf.setTotalPage(totalPage);
            sDaoSession.insertOrReplace(pdf);
        }
    }

    private DBManager() {
    }
}
