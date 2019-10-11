package com.aaron.yespdf.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.aaron.yespdf.common.bean.Collection;
import com.aaron.yespdf.common.bean.Cover;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.bean.RecentPDF;
import com.aaron.yespdf.common.event.ImportEvent;
import com.aaron.yespdf.common.greendao.CollectionDao;
import com.aaron.yespdf.common.greendao.DaoMaster;
import com.aaron.yespdf.common.greendao.DaoSession;
import com.aaron.yespdf.common.greendao.PDFDao;
import com.aaron.yespdf.common.greendao.RecentPDFDao;
import com.aaron.yespdf.common.utils.PdfUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class DBHelper {

    private static DaoSession sDaoSession;
//    private static boolean sSaveBitmapComplete;

    public static void init(Context context, String dbName) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, dbName);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        sDaoSession = daoMaster.newSession();
    }

    public static boolean updateDirName(String oldDirName, String newDirName) {
        if (StringUtils.isEmpty(newDirName)
                || oldDirName.equals(newDirName)) {
            return false;
        }
        PDFDao pdfDao = sDaoSession.getPDFDao();
        RecentPDFDao recentPDFDao = sDaoSession.getRecentPDFDao();
        CollectionDao collectionDao = sDaoSession.getCollectionDao();
        List<PDF> pdfList = pdfDao.queryBuilder()
                .where(PDFDao.Properties.Dir.eq(oldDirName))
                .list();
        List<RecentPDF> recentList = recentPDFDao.queryBuilder()
                .where(RecentPDFDao.Properties.Dir.eq(oldDirName))
                .list();
        List<Collection> collectionList = collectionDao.queryBuilder()
                .where(CollectionDao.Properties.Name.eq(oldDirName))
                .list();
        for (PDF pdf : pdfList) {
            pdf.setDir(newDirName);
        }
        for (RecentPDF recent : recentList) {
            recent.setDir(newDirName);
        }
        for (Collection c : collectionList) {
            c.setName(newDirName);
        }
        pdfDao.updateInTx(pdfList);
        recentPDFDao.updateInTx(recentList);
        collectionDao.updateInTx(collectionList);
        return true;
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
        if (StringUtils.isEmpty(dir)) {
            return new ArrayList<>();
        }
        return sDaoSession.queryRaw(PDF.class, "where DIR = ?", dir);
    }

    private static List<PDF> queryPDF(String dir, String name) {
        QueryBuilder<PDF> qb = sDaoSession.queryBuilder(PDF.class)
                .where(PDFDao.Properties.Dir.eq(dir), PDFDao.Properties.Name.eq(name))
                .orderDesc(PDFDao.Properties.LatestRead);
        return qb.list();
    }

    public static void insertPDFsToExist(List<String> pathList, String groupName) {
        for (String path : pathList) {
            insertPDFs(groupName, path);
        }
    }

    public static void insertRecent(PDF pdf) {
        String dir = pdf.getDir();
        String name = pdf.getName();
        RecentPDF recent = new RecentPDF(dir, name);
        sDaoSession.insertOrReplace(recent);
    }

    public static List<String> deleteRecent(List<PDF> list) {
        List<String> dirList = new ArrayList<>();
        for (PDF pdf : list) {
            dirList.add(pdf.getName());
            sDaoSession.getDatabase().execSQL("delete from RECENT_PDF where name = ?", new String[]{pdf.getName()});
        }
        return dirList;
    }

    public static String deleteCollection(String name) {
        sDaoSession.getDatabase().execSQL("delete from COLLECTION where name = ?", new String[]{name});
        return name;
    }

    public static List<String> deleteCollection(List<Cover> list) {
        List<String> dirList = new ArrayList<>();
        for (Cover c : list) {
            dirList.add(c.name);
//            sDaoSession.getCollectionDao().delete(c);
            sDaoSession.getDatabase().execSQL("delete from Collection where name = ?", new String[]{c.name});
            sDaoSession.getDatabase().execSQL("delete from PDF where dir = ?", new String[]{c.name});
            sDaoSession.getDatabase().execSQL("delete from RECENT_PDF where dir = ?", new String[]{c.name});
        }
        return dirList;
    }

    public static List<String> deletePDF(List<PDF> list) {
        List<String> nameList = new ArrayList<>();
        for (PDF pdf : list) {
            nameList.add(pdf.getName());
            sDaoSession.getPDFDao().delete(pdf);
        }
        return nameList;
    }

    public static boolean insert(List<String> pathList) {
        if (pathList == null || pathList.isEmpty()) {
            return false;
        }
        ImportEvent event = new ImportEvent();
        event.curProgress = 0;
        event.totalProgress = pathList.size();
        for (String path : pathList) {
            if (event.stop) return false;
            event.name = getName(path).substring(1);
            event.curProgress++;
            EventBus.getDefault().post(event);

            // 去除了文件名称的父路径
            String parentPath = path.substring(0, path.lastIndexOf("/"));
            LogUtils.e("parentPath: " + parentPath);
            // 所属文件夹
            String dir = parentPath.substring(parentPath.lastIndexOf("/") + 1);
            LogUtils.e("dir: " + dir);
            // 插入 Collection 对象
            Collection c = new Collection(dir);
            sDaoSession.insertOrReplace(c);

            insertPDFs(dir, path);

//            String bookmarkPage = "";
//            int curPage = 0;
//            int totalPage = PdfUtils.getPdfTotalPage(path);
//            String name = path.substring(path.lastIndexOf("/"), path.length() - 4);
//            String progress = "0.0%";
//            String cover = PathUtils.getInternalAppDataPath() + name + ".jpg";
//            // 制作 PDF 封面并缓存
//            long start = System.currentTimeMillis();
//            cover = PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(path, 0), cover);
//            long end = System.currentTimeMillis();
//            LogUtils.e(name + " cost: " + (end - start) + " milliseconds");
//
//            PDF pdf = new PDF();
//            pdf.setDir(dir);
//            pdf.setName(name.substring(1)); // 去除斜杠
//            pdf.setCover(cover);
//            pdf.setPath(path);
//            pdf.setProgress(progress);
//            pdf.setCurPage(curPage);
//            pdf.setBookmark(bookmarkPage);
//            pdf.setTotalPage(totalPage);
//            pdf.setLatestRead(0);
//            sDaoSession.insertOrReplace(pdf);
        }
        return true;
    }

    public static boolean insert(List<String> pathList, String groupName) {
        if (pathList == null || pathList.isEmpty()) return false;
//        String actualPath = pathList.get(0);
        // 去除了文件名称的父路径
//        String parentPath = actualPath.substring(0, actualPath.lastIndexOf("/"));
        // 所属文件夹
//        String name = parentPath.substring(parentPath.lastIndexOf("/") + 1);
        // 插入 Collection 对象
        Collection c = new Collection(groupName);
        sDaoSession.insertOrReplace(c);
        ImportEvent event = new ImportEvent();
        event.curProgress = 0;
        event.totalProgress = pathList.size();
        for (String path : pathList) {
            if (event.stop) return false;
            event.name = getName(path).substring(1);
            event.curProgress++;
            EventBus.getDefault().post(event);
            insertPDFs(groupName, path);
        }
        return true;
    }

    public static void insertNewCollection(String newDirName, List<PDF> pdfList) {
        Collection c = new Collection(newDirName);
        sDaoSession.insertOrReplace(c);
        transferPDFs(newDirName, pdfList);
    }

    public static void insertPDFsToCollection(String dirName, List<PDF> pdfList) {
        transferPDFs(dirName, pdfList);
    }

    private static boolean insertPDFs(String groupName, String filePath) {
        String bookmarkPage = "";
        int curPage = 0;
        int totalPage = PdfUtils.getPdfTotalPage(filePath);
        String name = getName(filePath);
        String progress = "0.0%";
        String cover = PathUtils.getInternalAppDataPath() + name + ".jpg";
        // 制作 PDF 封面并缓存
        long start = System.currentTimeMillis();
        cover = PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(filePath, 0), cover);
        long end = System.currentTimeMillis();
        LogUtils.e(name + " cost: " + (end - start) + " milliseconds");

        PDF pdf = new PDF();
        pdf.setDir(groupName);
        pdf.setName(name.substring(1)); // 去除斜杠
        pdf.setCover(cover);
        pdf.setPath(filePath);
        pdf.setProgress(progress);
        pdf.setCurPage(curPage);
        pdf.setBookmark(bookmarkPage);
        pdf.setTotalPage(totalPage);
        pdf.setLatestRead(0);
        sDaoSession.insertOrReplace(pdf);
        return true;
    }

    private static void transferPDFs(String dirName, List<PDF> pdfList) {
        for (PDF pdf : pdfList) {
            pdf.setDir(dirName);
        }
        sDaoSession.getPDFDao().updateInTx(pdfList);
    }

    private static String getName(String path) {
        return path.substring(path.lastIndexOf("/"), path.length() - 4);
    }

    private DBHelper() {
    }
}
