package com.aaron.yespdf.common

import android.content.Context
import com.aaron.yespdf.common.bean.Collection
import com.aaron.yespdf.common.bean.Cover
import com.aaron.yespdf.common.bean.PDF
import com.aaron.yespdf.common.bean.RecentPDF
import com.aaron.yespdf.common.event.ImportEvent
import com.aaron.yespdf.common.greendao.*
import com.aaron.yespdf.common.utils.PdfUtils
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
//        val helper = DevOpenHelper(context, dbName)
//        val db = helper.writableDatabase
//        val daoMaster = DaoMaster(db)
//        sDaoSession = daoMaster.newSession()

        val helper = UpdateOpenHelper(context, dbName, null)
        val db = helper.writableDatabase
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        val daoMaster = DaoMaster(db)
        sDaoSession = daoMaster.newSession()
    }

    fun migrate() {
        val collections = queryAllCollection()
        for (item in collections) {
            item.position = collections.indexOf(item)
            updateCollection(item)
            val pdfs = queryPDF(item.name)
            for (pdf in pdfs) {
                pdf.position = pdfs.indexOf(pdf)
                pdf.scaleFactor = 1.0f
                updatePDF(pdf)
            }
        }
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
        pdf?.run {
            sDaoSession.pdfDao.insertOrReplace(pdf)
        }
    }

    private fun updateCollection(c: Collection) {
        val collectionDao = sDaoSession.collectionDao
        collectionDao?.insertOrReplace(c)
    }

    fun updateCollection() {
        sDaoSession.collectionDao.updateInTx(DataManager.getCollectionList())
    }

    fun updatePDFs(pdfs: List<PDF>) {
        sDaoSession.pdfDao.updateInTx(pdfs)
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

    @JvmStatic
    fun queryAllCollection(): List<Collection> {
        val list = sDaoSession.loadAll<Collection, Any>(Collection::class.java)
        list.sortWith(Comparator { c1, c2 ->
            c2.position - c1.position
        })
        return list
    }

    @JvmStatic
    fun queryAllPDF(): List<PDF> {
        val list = sDaoSession.loadAll<PDF, Any>(PDF::class.java)
        list.sortWith(kotlin.Comparator { o1, o2 ->
            o2.position - o1.position
        })
        return list
    }

    @JvmStatic
    fun queryPDFByPath(path: String): PDF {
        return sDaoSession.pdfDao.queryRaw("where PATH = ?", path)[0]
    }

    private fun queryPDF(dir: String?): List<PDF> {
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
        val tempPath = pathList[0]
        val parentPath = tempPath.substring(0, tempPath.lastIndexOf("/"))
        val dir = tempPath.substring(0, tempPath.lastIndexOf("/")).substring(parentPath.lastIndexOf("/") + 1)
        var max = if (dir.isNotEmpty()) {
            queryPDF(dir).size
        } else 0
        for (path in pathList) {
            if (event.stop) return false
            event.name = getName(path)
            event.curProgress++
            EventBus.getDefault().post(event)
//            // 去除了文件名称的父路径
//            val parentPath = path.substring(0, path.lastIndexOf("/"))
//            // 所属文件夹
//            dir = parentPath.substring(parentPath.lastIndexOf("/") + 1)
            // 插入 Collection 对象
            val collectionList = DataManager.getCollectionList()
            val c = Collection(dir, collectionList.size)
            sDaoSession.insertOrReplace(c)
            insertPDFs(dir, path, max++)
        }
        return true
    }

    fun insertBackup(pdfs: List<PDF>, groupName: String) {
        if (pdfs.isEmpty()) {
            return
        }
        val c = Collection(groupName, DataManager.getCollectionList().size)
        sDaoSession.insertOrReplace(c)
        for (pdf in pdfs) {
            insertPDFs(groupName, "", 0, pdf)
        }
    }

    fun insert(pathList: List<String>?, groupName: String): Boolean {
        if (pathList == null || pathList.isEmpty()) return false
        // 插入 Collection 对象
        val c = Collection(groupName, DataManager.getCollectionList().size)
        sDaoSession.insertOrReplace(c)
        val event = ImportEvent()
        event.curProgress = 0
        event.totalProgress = pathList.size
        var max = queryPDF(groupName).size
        for (path in pathList) {
            if (event.stop) return false
            event.name = getName(path)
            event.curProgress++
            EventBus.getDefault().post(event)
            insertPDFs(groupName, path, max++)
        }
        return true
    }

    fun insertNewCollection(newDirName: String, pdfList: List<PDF>) {
        val c = Collection(newDirName, DataManager.getCollectionList().size)
        sDaoSession.insertOrReplace(c)
        transferPDFs(newDirName, pdfList)
    }

    fun insertPDFsToCollection(dirName: String, pdfList: List<PDF>) {
        transferPDFs(dirName, pdfList)
    }

    private fun insertPDFs(groupName: String, filePath: String, position: Int, pdf: PDF? = null): Boolean {
        val dir = pdf?.dir ?: groupName
        val name = pdf?.name ?: getName(filePath)
        val path = pdf?.path ?: filePath
        val cover = PdfUtils.saveBitmap(PdfUtils.pdfToBitmap(pdf?.path
                ?: filePath, 0), "${PathUtils.getInternalAppDataPath()}/$name.jpg")
        val progress = pdf?.progress ?: "0.0%"
        val curPage = pdf?.curPage ?: 0
        val bookmarkPage = pdf?.bookmark ?: ""
        val totalPage = pdf?.totalPage ?: PdfUtils.getPdfTotalPage(filePath)
        val latestRead = pdf?.latestRead ?: 0
        val pos = pdf?.position ?: position
        val scaleFactor = pdf?.scaleFactor ?: 1.0f
        PDF().apply {
            this.dir = dir
            this.name = name
            this.path = path
            this.cover = cover
            this.progress = progress
            this.curPage = curPage
            this.bookmark = bookmarkPage
            this.totalPage = totalPage
            this.latestRead = latestRead
            this.position = pos
            this.scaleFactor = scaleFactor
            sDaoSession.insertOrReplace(this)
        }
        return true
    }

    private fun transferPDFs(dirName: String, pdfList: List<PDF>) {
        for (pdf in pdfList) {
            pdf.dir = dirName
        }
        sDaoSession.pdfDao.updateInTx(pdfList)
    }

    private fun getName(path: String): String {
        return path.substring(path.lastIndexOf("/") + 1, path.length - 4)
    }
}