package com.aaron.yespdf.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.aaron.yespdf.common.bean.Collection;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.bean.RecentPDF;
import com.aaron.yespdf.common.greendao.DaoMaster;
import com.aaron.yespdf.common.greendao.DaoSession;
import com.aaron.yespdf.common.greendao.PDFDao;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class DBHelper {

    private static DaoSession sDaoSession;

    public static void init(Context context, String dbName) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, dbName);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        sDaoSession = daoMaster.newSession();
    }

    public static void updatePDF(PDF pdf) {
        sDaoSession.update(pdf);
    }

    public static List<PDF> queryRecentPDF() {
        List<RecentPDF> list = sDaoSession.loadAll(RecentPDF.class);
        List<PDF> recentPDFList = new ArrayList<>();
        for (RecentPDF recent : list) {
            recentPDFList.addAll(queryPDF(recent.getDir(), recent.getName()));
        }
        // 按最新阅读时间排序
        Collections.sort(recentPDFList, (p1, p2) -> (int) (p2.getLatestRead() - p1.getLatestRead()));
        return recentPDFList;
    }

    public static Collection queryCollection(String name) {
        List<Collection> list = sDaoSession.queryRaw(Collection.class, "where name = ?", name);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public static List<Collection> queryAllCollection() {
        return sDaoSession.loadAll(Collection.class);
    }

    public static List<PDF> queryAllPDF() {
        return sDaoSession.loadAll(PDF.class);
    }

    public static List<PDF> queryPDF(String dir) {
        return sDaoSession.queryRaw(PDF.class, "where DIR = ?", dir);
    }

    private static List<PDF> queryPDF(String dir, String name) {
        QueryBuilder<PDF> qb = sDaoSession.queryBuilder(PDF.class)
                .where(PDFDao.Properties.Dir.eq(dir), PDFDao.Properties.Name.eq(name))
                .orderDesc(PDFDao.Properties.LatestRead);
        return qb.list();
    }

    public static void insertRecent(PDF pdf) {
        String dir = pdf.getDir();
        String name = pdf.getName();
        RecentPDF recent = new RecentPDF(dir, name);
        sDaoSession.insertOrReplace(recent);
    }

    public static void insert(List<String> pathList) {
        int count = pathList.size();
        if (count == 0) return;

        // 实际文件路径与文件名称
        String actualPath1 = pathList.get(0);
        String actualPath2 = count >= 2 ? pathList.get(1) : null;
        String actualPath3 = count >= 3 ? pathList.get(2) : null;
        String actualPath4 = count >= 4 ? pathList.get(3) : null;
        String fileName1   = actualPath1.substring(actualPath1.lastIndexOf("/"), actualPath1.length() - 4);
        String fileName2   = actualPath2 != null ? actualPath2.substring(actualPath2.lastIndexOf("/"), actualPath2.length() - 4) : null;
        String fileName3   = actualPath3 != null ? actualPath3.substring(actualPath3.lastIndexOf("/"), actualPath3.length() - 4) : null;
        String fileName4   = actualPath4 != null ? actualPath4.substring(actualPath4.lastIndexOf("/"), actualPath4.length() - 4) : null;

        // 去除了文件名称的父路径
        String parentPath = actualPath1.substring(0, actualPath1.lastIndexOf("/"));
        // Collection 属性
        String name   = parentPath.substring(parentPath.lastIndexOf("/") + 1);
        String cover1 = PathUtils.getInternalAppCachePath() + fileName1 + ".jpg";
        String cover2 = fileName2 != null ? PathUtils.getInternalAppCachePath() + fileName2 + ".jpg" : "";
        String cover3 = fileName3 != null ? PathUtils.getInternalAppCachePath() + fileName3 + ".jpg" : "";
        String cover4 = fileName4 != null ? PathUtils.getInternalAppCachePath() + fileName4 + ".jpg" : "";
        // 制作 PDF 封面并缓存
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Object>() {
            @Nullable
            @Override
            public Object doInBackground() throws Throwable {
                PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(actualPath1, 0), cover1);
                PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(actualPath2, 0), cover2);
                PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(actualPath3, 0), cover3);
                PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(actualPath4, 0), cover4);
                return null;
            }

            @Override
            public void onSuccess(@Nullable Object result) {

            }
        });

        Collection exist = queryCollection(name);
        int existCount = 0;
        String existCover1 = null;
        String existCover2 = null;
        String existCover3 = null;
        String existCover4 = null;
        if (exist != null) {
            existCount = exist.getCount();
            existCover1 = exist.getCover1();
            existCover2 = exist.getCover2();
            existCover3 = exist.getCover3();
            existCover4 = exist.getCover4();
        }
        Collection collection = new Collection();
        collection.setName(name);
        collection.setCount(count + existCount);
        switch (existCount) {
            case 0:
                collection.setCover1(StringUtils.isEmpty(existCover1) ? cover1 : existCover1);
                break;
            case 1:
                collection.setCover1(existCover1);
                collection.setCover2(StringUtils.isEmpty(existCover2) ? cover1 : existCover2);
                break;
            case 2:
                collection.setCover1(existCover1);
                collection.setCover2(existCover2);
                collection.setCover3(StringUtils.isEmpty(existCover3) ? cover1 : existCover3);
                break;
            case 3:
                collection.setCover1(existCover1);
                collection.setCover2(existCover2);
                collection.setCover3(existCover3);
                collection.setCover4(StringUtils.isEmpty(existCover4) ? cover1 : existCover4);
                break;
        }
        sDaoSession.insertOrReplace(collection);
        insertPDFs(name, pathList);
    }

    private static void insertPDFs(String dir, List<String> pathList) {
        for (String path : pathList) {
            String bookmarkPage = "";
            int curPage         = 0;
            int totalPage       = PdfUtils.getPdfTotalPage(path);
            String name         = path.substring(path.lastIndexOf("/"), path.length() - 4);
            String progress     = "0.0%";
            String cover        = PathUtils.getInternalAppCachePath() + name + ".jpg";
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
            pdf.setName(name.substring(1)); // 去除斜杠
            pdf.setCover(cover);
            pdf.setPath(path);
            pdf.setProgress(progress);
            pdf.setCurPage(curPage);
            pdf.setBookmarkPage(bookmarkPage);
            pdf.setTotalPage(totalPage);
            pdf.setLatestRead(0);
            sDaoSession.insertOrReplace(pdf);
        }
    }

    private DBHelper() {
    }
}
