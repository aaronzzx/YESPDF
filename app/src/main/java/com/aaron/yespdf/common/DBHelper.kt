package com.aaron.yespdf.common

import android.content.Context
import com.aaron.yespdf.common.bean.Collection
import com.aaron.yespdf.common.bean.Cover
import com.aaron.yespdf.common.bean.PDF
import com.aaron.yespdf.common.bean.RecentPDF
import com.aaron.yespdf.common.event.ImportEvent
import com.aaron.yespdf.common.greendao.*
import com.aaron.yespdf.common.greendao.DaoMaster.DevOpenHelper
import com.aaron.yespdf.common.utils.PdfUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.StringUtils
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
object DBHelper {

    private lateinit var sDaoSession: DaoSession

    @JvmStatic
    fun init(context: Context, dbName: String) {
        val helper = DevOpenHelper(context, dbName)
        val db = helper.writableDatabase
        val daoMaster = DaoMaster(db)
        sDaoSession = daoMaster.newSession()
    }

    fun updateDirName(oldDirName: String, newDirName: String): Boolean {
        if (StringUtils.isEmpty(newDirName)
                || oldDirName == newDirName) {
            return false
        }
        val pdfDao = sDaoSession.pdfDao
        val recentPDFDao = sDaoSession.recentPDFDao
        val collectionDao = sDaoSession.collectionDao
        val pdfList = pdfDao.queryBuilder()
                .where(PDFDao.Properties.Dir.eq(oldDirName))
                .list()
        val recentList = recentPDFDao.queryBuilder()
                .where(RecentPDFDao.Properties.Dir.eq(oldDirName))
                .list()
        val collectionList = collectionDao.queryBuilder()
                .where(CollectionDao.Properties.Name.eq(oldDirName))
                .list()
        for (pdf in pdfList) {
            pdf.dir = newDirName
        }
        for (recent in recentList) {
            recent.dir = newDirName
        }
        for (c in collectionList) {
            c.name = newDirName
        }
        pdfDao.updateInTx(pdfList)
        recentPDFDao.updateInTx(recentList)
        collectionDao.updateInTx(collectionList)
        return true
    }

    fun updatePDF(pdf: PDF?) {
        pdf?.run { sDaoSession.update(this) }
    }

    fun updateCollection(name: String) {
        val collectionDao = sDaoSession.collectionDao
        collectionDao?.insertOrReplace(Collection(name))
    }

    @JvmStatic
    fun queryRecentPDF(): List<PDF> {
        val list = sDaoSession.loadAll<RecentPDF, Any>(RecentPDF::class.java)
        val recentPDFList: MutableList<PDF> = ArrayList()
        for (recent in list) {
            recentPDFList.addAll(queryPDF(recent.dir, recent.name))
        }
        // 按最新阅读时间排序
        recentPDFList.sortWith(Comparator { p1: PDF, p2: PDF -> (p2.latestRead - p1.latestRead).toInt() })
        return recentPDFList
    }

    fun queryCollection(name: String?): Collection? {
        val list = sDaoSession.queryRaw<Collection, Any>(Collection::class.java, "where name = ?", name)
        return if (list.isEmpty()) {
            null
        } else list[0]
    }

    @JvmStatic
    fun queryAllCollection(): List<Collection> {
        return sDaoSession.loadAll<Collection, Any>(Collection::class.java)
    }

    @JvmStatic
    fun queryAllPDF(): List<PDF> {
        return sDaoSession.loadAll<PDF, Any>(PDF::class.java)
    }

    fun queryPDF(dir: String?): List<PDF> {
        return if (StringUtils.isEmpty(dir)) {
            ArrayList()
        } else sDaoSession.queryRaw<PDF, Any>(PDF::class.java, "where DIR = ?", dir)
    }

    private fun queryPDF(dir: String, name: String): List<PDF> {
        val qb = sDaoSession.queryBuilder(PDF::class.java)
                .where(PDFDao.Properties.Dir.eq(dir), PDFDao.Properties.Name.eq(name))
                .orderDesc(PDFDao.Properties.LatestRead)
        return qb.list()
    }

    fun insertPDFsToExist(pathList: List<String>, groupName: String) {
        for (path in pathList) {
            insertPDFs(groupName, path)
        }
    }

    fun insertRecent(pdf: PDF) {
        val dir = pdf.dir
        val name = pdf.name
        val recent = RecentPDF(dir, name)
        sDaoSession.insertOrReplace(recent)
    }

    fun deleteRecent(list: List<PDF>): List<String> {
        val dirList: MutableList<String> = ArrayList()
        for (pdf in list) {
            dirList.add(pdf.name)
            sDaoSession.database.execSQL("delete from RECENT_PDF where name = ?", arrayOf(pdf.name))
        }
        return dirList
    }

    fun deleteCollection(name: String?): String? {
        sDaoSession.database.execSQL("delete from COLLECTION where name = ?", arrayOf(name))
        return name
    }

    fun deleteCollection(list: List<Cover>): List<String> {
        val dirList: MutableList<String> = ArrayList()
        for (c in list) {
            dirList.add(c.name)
            sDaoSession.database.execSQL("delete from Collection where name = ?", arrayOf(c.name))
            sDaoSession.database.execSQL("delete from PDF where dir = ?", arrayOf(c.name))
            sDaoSession.database.execSQL("delete from RECENT_PDF where dir = ?", arrayOf(c.name))
        }
        return dirList
    }

    fun deletePDF(list: List<PDF>?): List<String> {
        val nameList: MutableList<String> = ArrayList()
        if (list != null) {
            for (pdf in list) {
                nameList.add(pdf.name)
                sDaoSession.pdfDao.delete(pdf)
            }
        }
        return nameList
    }

    fun insert(pathList: List<String>?): Boolean {
        if (pathList == null || pathList.isEmpty()) {
            return false
        }
        val event = ImportEvent()
        event.curProgress = 0
        event.totalProgress = pathList.size
        for (path in pathList) {
            if (event.stop) return false
            event.name = getName(path).substring(1)
            event.curProgress++
            EventBus.getDefault().post(event)
            // 去除了文件名称的父路径
            val parentPath = path.substring(0, path.lastIndexOf("/"))
            // 所属文件夹
            val dir = parentPath.substring(parentPath.lastIndexOf("/") + 1)
            // 插入 Collection 对象
            val c = Collection(dir)
            sDaoSession.insertOrReplace(c)
            insertPDFs(dir, path)
        }
        return true
    }

    fun insert(pathList: List<String>?, groupName: String): Boolean {
        if (pathList == null || pathList.isEmpty()) return false
        // 插入 Collection 对象
        val c = Collection(groupName)
        sDaoSession.insertOrReplace(c)
        val event = ImportEvent()
        event.curProgress = 0
        event.totalProgress = pathList.size
        for (path in pathList) {
            if (event.stop) return false
            event.name = getName(path).substring(1)
            event.curProgress++
            EventBus.getDefault().post(event)
            insertPDFs(groupName, path)
        }
        return true
    }

    fun insertNewCollection(newDirName: String, pdfList: List<PDF>) {
        val c = Collection(newDirName)
        sDaoSession.insertOrReplace(c)
        transferPDFs(newDirName, pdfList)
    }

    fun insertPDFsToCollection(dirName: String, pdfList: List<PDF>) {
        transferPDFs(dirName, pdfList)
    }

    private fun insertPDFs(groupName: String, filePath: String): Boolean {
        val bookmarkPage = ""
        val curPage = 0
        val totalPage = PdfUtils.getPdfTotalPage(filePath)
        val name = getName(filePath)
        val progress = "0.0%"
        var cover: String? = PathUtils.getInternalAppDataPath() + name + ".jpg"
        // 制作 PDF 封面并缓存
        val start = System.currentTimeMillis()
        cover = PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(filePath, 0), cover)
        val end = System.currentTimeMillis()
        LogUtils.e(name + " cost: " + (end - start) + " milliseconds")
        val pdf = PDF()
        pdf.dir = groupName
        pdf.name = name.substring(1) // 去除斜杠
        pdf.cover = cover
        pdf.path = filePath
        pdf.progress = progress
        pdf.curPage = curPage
        pdf.bookmark = bookmarkPage
        pdf.totalPage = totalPage
        pdf.latestRead = 0
        sDaoSession.insertOrReplace(pdf)
        return true
    }

    private fun transferPDFs(dirName: String, pdfList: List<PDF>) {
        for (pdf in pdfList) {
            pdf.dir = dirName
        }
        sDaoSession.pdfDao.updateInTx(pdfList)
    }

    private fun getName(path: String): String {
        return path.substring(path.lastIndexOf("/"), path.length - 4)
    }
}