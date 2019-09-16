package com.aaron.yespdf.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.aaron.yespdf.common.bean.Collection;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.bean.RecentPDF;
import com.aaron.yespdf.common.greendao.DaoMaster;
import com.aaron.yespdf.common.greendao.DaoSession;
import com.aaron.yespdf.common.greendao.PDFDao;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;

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
    private static boolean sSaveBitmapComplete;

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

    public static boolean insert(List<String> pathList) {
        if (pathList == null || pathList.isEmpty()) return false;
        String actualPath = pathList.get(0);
        // 去除了文件名称的父路径
        String parentPath = actualPath.substring(0, actualPath.lastIndexOf("/"));
        // 所属文件夹
        String name = parentPath.substring(parentPath.lastIndexOf("/") + 1);
        // 插入 Collection 对象
        Collection c = new Collection(name);
        sDaoSession.insertOrReplace(c);
        return insertPDFs(name, pathList);
    }

    private static boolean insertPDFs(String dir, List<String> pathList) {
        for (String path : pathList) {
            String bookmarkPage = "";
            int curPage         = 0;
            int totalPage       = PdfUtils.getPdfTotalPage(path);
            String name         = path.substring(path.lastIndexOf("/"), path.length() - 4);
            String progress     = "0.0%";
            String cover = PathUtils.getInternalAppDataPath() + name + ".jpg";
            // 制作 PDF 封面并缓存
            try {
                long start = System.currentTimeMillis();
                PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(path, 0), cover);
                long end = System.currentTimeMillis();
                LogUtils.e(name + " cost: " + (end - start) + " milliseconds");
                sSaveBitmapComplete = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!sSaveBitmapComplete) {
                LogUtils.v("Saving PDF cover, Wait a moment.");
            }

            PDF pdf = new PDF();
            pdf.setDir(dir);
            pdf.setName(name.substring(1)); // 去除斜杠
            pdf.setCover(cover);
            pdf.setPath(path);
            pdf.setProgress(progress);
            pdf.setCurPage(curPage);
            pdf.setBookmark(bookmarkPage);
            pdf.setTotalPage(totalPage);
            pdf.setLatestRead(0);
            sDaoSession.insertOrReplace(pdf);
            sSaveBitmapComplete = false;
        }
        return true;
    }

    private DBHelper() {
    }
}
