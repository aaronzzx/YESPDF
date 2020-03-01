package com.aaron.yespdf.preview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentPagerAdapter
import com.aaron.base.impl.OnClickListenerImpl
import com.aaron.base.impl.TextWatcherImpl
import com.aaron.yespdf.R
import com.aaron.yespdf.common.*
import com.aaron.yespdf.common.bean.PDF
import com.aaron.yespdf.common.event.RecentPDFEvent
import com.aaron.yespdf.common.utils.AboutUtils
import com.aaron.yespdf.common.utils.PdfUtils
import com.aaron.yespdf.preview.PreviewActivity
import com.aaron.yespdf.settings.SettingsActivity.Companion.start
import com.blankj.utilcode.util.*
import com.github.barteksc.pdfviewer.PDFView.Configurator
import com.google.gson.reflect.TypeToken
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfPasswordException
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_activity_preview.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * 注意点：显示到界面上的页数需要加 1 ，因为 PDFView 获取到的页数是从 0 计数的。
 */
class PreviewActivity : CommonActivity(), IActivityInterface {

    private var pdf: PDF? = null // 本应用打开
    private var uri: Uri? = null // 一般是外部应用打开
    private var curPage = 0
    private var pageCount = 0
    private var password: String? = null
    private var isNightMode = Settings.isNightMode()
    private var isVolumeControl = Settings.isVolumeControl()
    private var contentFragInterface: IContentFragInterface? = null
    private var bkFragInterface: IBkFragInterface? = null
    private var autoDisp: Disposable? = null // 自动滚动
    private var isPause = false
    private var hideBar = false
    private val contentMap: MutableMap<Long, PdfDocument.Bookmark> = HashMap()
    private val bookmarkMap: MutableMap<Long, Bookmark> = HashMap()
    private val pageList: MutableList<Long> = ArrayList()

    private var isScrollLevelTouchFinish = true
    private var previousPage = 0 // 记录 redo/undo的页码
    private var nextPage = 0
    private var canvas: Canvas? = null // AndroidPDFView 的画布
    private var paint: Paint? = null // 画书签的画笔
    private var pageWidth = 0F
    private val sbScrollLevel: SeekBar by lazy { findViewById<SeekBar>(R.id.app_sb_scroll_level) }
    private val alertDialog: Dialog by lazy(LazyThreadSafetyMode.NONE) {
        DialogManager.createAlertDialog(this) { tvTitle, tvContent, btn ->
            tvTitle.setText(R.string.app_oop_error)
            tvContent.setText(R.string.app_doc_parse_error)
            btn.setText(R.string.app_exit_cur_content)
            btn.setOnClickListener { finish() }
        }
    }
    private val inputDialog: Dialog by lazy(LazyThreadSafetyMode.NONE) {
        DialogManager.createInputDialog(this) { tvTitle, etInput, btnLeft, btnRight ->
            tvTitle.setText(R.string.app_need_verify_password)
            etInput.addTextChangedListener(object : TextWatcherImpl() {
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    password = charSequence.toString()
                }
            })
            btnLeft.setText(R.string.app_do_not_delete)
            btnLeft.setOnClickListener { finish() }
            btnRight.setText(R.string.app_confirm)
            btnRight.setOnClickListener {
                initPdf(uri, pdf)
                inputDialog.dismiss()
            }
        }
    }

    override fun layoutId(): Int {
        return R.layout.app_activity_preview
    }

    override fun createToolbar(): Toolbar? {
        return findViewById(R.id.app_toolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView(savedInstanceState)
    }

    override fun onRestart() {
        LogUtils.e("onRestart")
        super.onRestart()
        // 解决锁定横屏时从后台回到前台时头两次点击无效
        val screenHeight = ScreenUtils.getScreenHeight()
        app_ll_read_method.translationY = screenHeight.toFloat()
        app_ll_more.translationY = screenHeight.toFloat()
        enterFullScreen() // 重新回到界面时主动进入全屏
    }

    override fun onPause() {
        super.onPause()
        pdf?.run {
            val progress = getPercent(curPage + 1, pageCount)
            this.curPage = this@PreviewActivity.curPage
            this.progress = progress
            bookmark = GsonUtils.toJson(bookmarkMap.values)
            DBHelper.updatePDF(this)
            DataManager.updatePDFs()
            // 这里发出事件主要是更新界面阅读进度
            EventBus.getDefault().post(RecentPDFEvent(true))
        }
    }

    override fun onStop() {
        super.onStop()
        hideBar()
        // 书签页回原位
        app_ll_content.translationX = -app_ll_content.measuredWidth.toFloat()
        app_screen_cover.alpha = 0f // 隐藏界面遮罩
        // 阅读方式回原位
        app_ll_read_method.translationY = ScreenUtils.getScreenHeight().toFloat()
        app_ll_more.translationY = ScreenUtils.getScreenHeight().toFloat()
        if (autoDisp?.isDisposed == false) {
            autoDisp?.dispose()
            sbScrollLevel.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        app_pdfview.recycle()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            if (Settings.isKeepScreenOn()) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(BUNDLE_CUR_PAGE, curPage)
        outState.putString(BUNDLE_PASSWORD, password)
    }

    /**
     * 获取阅读进度的百分比
     */
    private fun getPercent(percent: Int, total: Int): String { // 创建一个数值格式化对象
        val numberFormat = NumberFormat.getInstance()
        // 设置精确到小数点后2位
        numberFormat.maximumFractionDigits = 1
        //计算x年x月的成功率
        val result = numberFormat.format(percent.toFloat() / total.toFloat() * 100.toDouble())
        return "$result%"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onBackPressed() {
        when {
            app_ll_content.translationX == 0f -> { // 等于 0 表示正处于打开状态，需要隐藏
                closeContent(null)
            }
            app_ll_read_method.translationY != ScreenUtils.getScreenHeight().toFloat() -> {
                // 不等于屏幕高度表示正处于显示状态，需要隐藏
                closeReadMethod()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                && autoDisp?.isDisposed == false) {
            exitFullScreen()
            showBar()
            return true
        } else if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                && ScreenUtils.isPortrait() && isVolumeControl && toolbar?.alpha == 0.0f) {
            // 如果非全屏状态是无法使用音量键翻页的
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    var currentPage1 = app_pdfview.currentPage
                    app_pdfview.jumpTo(--currentPage1, true)
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    var currentPage2 = app_pdfview.currentPage
                    app_pdfview.jumpTo(++currentPage2, true)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SETTINGS) {
            isVolumeControl = Settings.isVolumeControl()
            if (isNightMode != Settings.isNightMode()) {
                isNightMode = Settings.isNightMode()
                initPdf(uri, pdf)
            }
        }
    }

    override fun onJumpTo(page: Int) {
        app_pdfview_bg.visibility = View.VISIBLE
        closeContent(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                app_pdfview.jumpTo(page)
            }
        })
    }

    @SuppressLint("SwitchIntDef")
    private fun initView(savedInstanceState: Bundle?) {
        if (isNightMode) {
            app_pdfview_bg.background = ColorDrawable(Color.BLACK)
        }
        supportActionBar?.run {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.app_ic_action_back_white)
        }
        if (!Settings.isSwipeHorizontal()) {
            val lp = app_pdfview_bg.layoutParams
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
            app_pdfview_bg.layoutParams = lp
        }
        // 移动到屏幕下方
        app_ll_read_method.translationY = ScreenUtils.getScreenHeight().toFloat()
        app_ll_more.translationY = ScreenUtils.getScreenHeight().toFloat()
        // 移动到屏幕左边
        app_ll_content.post {
            if (app_ll_content != null) {
                app_ll_content.translationX = -app_ll_content.measuredWidth.toFloat()
            }
        }
        if (Settings.isSwipeHorizontal()) {
            app_tv_horizontal.setTextColor(resources.getColor(R.color.app_color_accent))
        } else {
            app_tv_vertical.setTextColor(resources.getColor(R.color.app_color_accent))
        }
        if (Settings.isLockLandscape()) {
            app_tv_lock_landscape.setTextColor(resources.getColor(R.color.app_color_accent))
            if (!ScreenUtils.isLandscape()) ScreenUtils.setLandscape(this)
        } else {
            app_tv_lock_landscape.setTextColor(Color.WHITE)
        }
        // 目录书签侧滑页初始化
        val fm = supportFragmentManager
        val adapter: FragmentPagerAdapter = PagerAdapter(fm)
        app_vp.adapter = adapter
        app_tab_layout.setupWithViewPager(app_vp)
        val tab1 = app_tab_layout.getTabAt(0)
        val tab2 = app_tab_layout.getTabAt(1)
        tab1?.setCustomView(R.layout.app_tab_content)
        tab2?.setCustomView(R.layout.app_tab_bookmark)
        getData(savedInstanceState)
        setListener()
        initPdf(uri, pdf)
        enterFullScreen()
    }

    private fun getData(savedInstanceState: Bundle?) {
        val intent = intent
        uri = intent.data
        pdf = intent.getParcelableExtra(EXTRA_PDF)
        if (pdf != null) {
            curPage = savedInstanceState?.getInt(BUNDLE_CUR_PAGE) ?: pdf?.curPage ?: 0
            password = savedInstanceState?.getString(BUNDLE_PASSWORD)
            pageCount = pdf?.totalPage ?: 0
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListener() {
        app_ll_bottombar.setOnClickListener { }
        app_ibtn_quickbar_action.setOnClickListener {
            // 当前页就是操作后的上一页或者下一页
            if (it.isSelected) {
                previousPage = app_pdfview.currentPage
                app_pdfview.jumpTo(nextPage) // Redo
            } else {
                nextPage = app_pdfview.currentPage
                app_pdfview.jumpTo(previousPage) // Undo
            }
            it.isSelected = !it.isSelected
        }
        app_tv_previous_chapter.setOnClickListener {
            // 减 1 是为了防止当前页面有标题的情况下无法跳转，因为是按标题来跳转
            if (hideBar) {
                return@setOnClickListener   // 防误触
            }
            var targetPage = app_pdfview.currentPage - 1
            if (pageList.isNotEmpty() && targetPage >= pageList[0]) {
                if (app_ll_undoredobar.visibility != View.VISIBLE) {
                    showQuickbar()
                }
                app_ibtn_quickbar_action.isSelected = false // 将状态调为 Undo
                while (!pageList.contains(targetPage.toLong())) {
                    if (targetPage < pageList[0]) {
                        return@setOnClickListener   // 如果实在匹配不到就跳出方法，不执行跳转
                    }
                    targetPage-- // 如果匹配不到会一直减 1 搜索
                }
                previousPage = app_pdfview.currentPage
                app_pdfview.jumpTo(targetPage)
            }
        }
        app_tv_next_chapter.setOnClickListener {
            if (hideBar) {
                return@setOnClickListener   // 防误触
            }
            // 这里的原理和上面跳转上一章节一样
            var targetPage = app_pdfview.currentPage + 1
            if (pageList.isNotEmpty() && targetPage <= pageList[pageList.size - 1]) {
                app_pdfview_bg.visibility = View.VISIBLE
                if (app_ll_undoredobar.visibility != View.VISIBLE) {
                    showQuickbar()
                }
                app_ibtn_quickbar_action.isSelected = false
                while (!pageList.contains(targetPage.toLong())) {
                    if (targetPage > pageList[pageList.size - 1]) {
                        return@setOnClickListener
                    }
                    targetPage++
                }
                previousPage = app_pdfview.currentPage
                app_pdfview.jumpTo(targetPage)
            }
        }
        app_tv_content.setOnClickListener(object : OnClickListenerImpl() {
            override fun onViewClick(v: View, interval: Long) {
                val tab = app_tab_layout.getTabAt(0)
                tab?.select()
                hideBar()
                enterFullScreen()
                openContent()
            }
        })
        app_tv_read_method.setOnClickListener(object : OnClickListenerImpl() {
            override fun onViewClick(v: View, interval: Long) {
                hideBar()
                enterFullScreen()
                openReadMethod()
            }
        })
        app_tv_auto_scroll.setOnClickListener {
            if (Settings.isSwipeHorizontal()) {
                UiManager.showCenterShort(R.string.app_horizontal_does_not_support_auto_scroll)
                return@setOnClickListener
            }
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                hideBar()
                enterFullScreen()
                sbScrollLevel.progress = Settings.getScrollLevel().toInt() - 1
                sbScrollLevel.visibility = View.VISIBLE
                autoDisp = startAutoScroll()
            } else {
                if (autoDisp?.isDisposed == false) {
                    autoDisp?.dispose()
                }
                sbScrollLevel.visibility = View.GONE
            }
        }
        sbScrollLevel.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isScrollLevelTouchFinish = false
                sbScrollLevel.alpha = 1.0f
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isScrollLevelTouchFinish = true
                sbScrollLevel.animate()
                        .alpha(0.4f)
                        .setDuration(200L)
                        .setUpdateListener {
                            if (!isScrollLevelTouchFinish) {
                                sbScrollLevel.animate().cancel()
                                sbScrollLevel.alpha = 1.0f
                            }
                        }
                        .start()
                Settings.setScrollLevel((sbScrollLevel.progress + 1).toLong())
                autoDisp?.dispose()
                app_tv_auto_scroll.isSelected = true
                autoDisp = startAutoScroll()
            }
        })
        app_tv_bookmark.setOnClickListener {
            it.isSelected = !it.isSelected
            val curPage = app_pdfview.currentPage
            if (it.isSelected) {
                val title = getTitle(curPage)
                val time = System.currentTimeMillis()
                val bk = Bookmark(curPage, title, time)
                bookmarkMap[curPage.toLong()] = bk
            } else {
                bookmarkMap.remove(curPage.toLong())
            }
            bkFragInterface?.update(bookmarkMap.values)
            app_pdfview.invalidate()
        }
        app_tv_bookmark.setOnLongClickListener {
            val tab = app_tab_layout.getTabAt(1)
            tab?.select()
            hideBar()
            enterFullScreen()
            openContent()
            true
        }
        app_tv_more.setOnClickListener(object : OnClickListenerImpl() {
            override fun onViewClick(v: View, interval: Long) {
                hideBar()
                enterFullScreen()
                openMore()
            }
        })
        app_tv_lock_landscape.setOnClickListener {
            if (ScreenUtils.isPortrait()) {
                Settings.setLockLandscape(true)
                ScreenUtils.setLandscape(this)
            } else {
                Settings.setLockLandscape(false)
                ScreenUtils.setPortrait(this)
            }
        }
        app_tv_export_image.setOnClickListener {
            closeMore()
            if (pdf != null) {
                launch {
                    withContext(Dispatchers.IO) {
                        val bmp = PdfUtils.pdfToBitmap(pdf!!.path, curPage)
                        val path = "${PathUtils.getExternalPicturesPath()}/YESPDF/${pdf!!.name}/第${curPage + 1}页.png"
                        AboutUtils.copyImageToDevice(this@PreviewActivity, bmp, path)
                    }
                    UiManager.showShort(R.string.app_export_image_succeed)
                }
            } else UiManager.showShort(R.string.app_not_support_external_open)
        }
        app_tv_settings.setOnClickListener(object : OnClickListenerImpl() {
            override fun onViewClick(v: View, interval: Long) {
                hideBar()
                if (autoDisp?.isDisposed == false) {
                    autoDisp?.dispose()
                }
                start(this@PreviewActivity, REQUEST_CODE_SETTINGS)
            }
        })
        app_screen_cover.setOnTouchListener { _: View?, _: MotionEvent? ->
            if (app_ll_content.translationX == 0f) {
                closeContent(null)
                return@setOnTouchListener true
            }
            false
        }
        app_tv_horizontal.setOnClickListener {
            closeReadMethod()
            if (!Settings.isSwipeHorizontal()) {
                if (autoDisp?.isDisposed == false) {
                    autoDisp?.dispose()
                    UiManager.showCenterShort(R.string.app_horizontal_does_not_support_auto_scroll)
                    return@setOnClickListener
                }
                app_tv_horizontal.setTextColor(resources.getColor(R.color.app_color_accent))
                app_tv_vertical.setTextColor(resources.getColor(R.color.base_white))
                Settings.setSwipeHorizontal(true)
                initPdf(uri, pdf)
            }
        }
        app_tv_vertical.setOnClickListener {
            closeReadMethod()
            if (Settings.isSwipeHorizontal()) {
                app_tv_vertical.setTextColor(resources.getColor(R.color.app_color_accent))
                app_tv_horizontal.setTextColor(resources.getColor(R.color.base_white))
                Settings.setSwipeHorizontal(false)
                initPdf(uri, pdf)
            }
        }
    }

    private fun startAutoScroll(): Disposable {
        return Observable.interval(Settings.getScrollLevel(), TimeUnit.MILLISECONDS)
                .doOnDispose {
                    app_tv_auto_scroll.isSelected = false
                    isPause = false
                }
                .doOnSubscribe {
                    app_tv_auto_scroll.isSelected = true
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this@PreviewActivity)))
                .subscribe {
                    if (!app_pdfview.isRecycled && !isPause) {
                        app_pdfview.moveRelativeTo(0f, OFFSET_Y)
                        app_pdfview.loadPageByOffset()
                    }
                }
    }

    private fun openReadMethod() {
        val screenHeight = ScreenUtils.getScreenHeight()
        var viewHeight = app_ll_read_method.measuredHeight
        if (app_ll_read_method.translationY < viewHeight) {
            viewHeight += ConvertUtils.dp2px(24f)
        }
        app_ll_read_method.animate()
                .setDuration(200)
                .setStartDelay(100)
                .translationY(screenHeight - viewHeight.toFloat())
                .start()
    }

    private fun closeReadMethod() {
        val screenHeight = ScreenUtils.getScreenHeight()
        app_ll_read_method.animate()
                .setDuration(200)
                .translationY(screenHeight.toFloat())
                .start()
    }

    private fun openMore() {
        val screenHeight = ScreenUtils.getScreenHeight()
        var viewHeight = app_ll_more.measuredHeight
        if (app_ll_more.translationY < viewHeight) {
            viewHeight += ConvertUtils.dp2px(24f)
        }
        app_ll_more.animate()
                .setDuration(200)
                .setStartDelay(100)
                .translationY(screenHeight - viewHeight.toFloat())
                .start()
    }

    private fun closeMore() {
        val screenHeight = ScreenUtils.getScreenHeight()
        app_ll_more.animate()
                .setDuration(200)
                .translationY(screenHeight.toFloat())
                .start()
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun initPdf(uri: Uri?, pdf: PDF?) {
        app_sb_progress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                app_tv_pageinfo.text = (i + 1).toString() + " / " + pageCount
                // Quickbar
                app_quickbar_title.text = getTitle(i)
                app_tv_pageinfo2.text = (i + 1).toString() + " / " + pageCount
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                app_ibtn_quickbar_action.isSelected = false
                previousPage = seekBar.progress
                if (!hideBar) {
                    showQuickbar()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                nextPage = seekBar.progress
                app_pdfview.jumpTo(seekBar.progress)
            }
        })
        var configurator: Configurator
        when {
            uri != null -> {
                val file = UriUtils.uri2File(uri)
                val path = if (file != null) UriUtils.uri2File(uri).absolutePath else null
                app_tv_pageinfo.text = "1 / $pageCount"
                val bookName = path?.substring(path.lastIndexOf("/") + 1, path.length - 4) ?: ""
                toolbar?.post { toolbar?.title = bookName }
                configurator = app_pdfview.fromUri(uri).defaultPage(curPage)
            }
            pdf != null -> {
                val bkJson = pdf.bookmark // 获取书签 json
                val bkList = GsonUtils.fromJson<List<Bookmark>>(bkJson, object : TypeToken<List<Bookmark?>?>() {}.type)
                if (bkList != null) {
                    for (bk in bkList) {
                        LogUtils.e(bk)
                        val pageId = bk.pageId
                        bookmarkMap[pageId.toLong()] = bk
                    }
                }
                app_sb_progress.progress = curPage
                app_tv_pageinfo.text = (curPage + 1).toString() + " / " + pageCount
                toolbar?.post { toolbar?.title = pdf.name }
                configurator = app_pdfview.fromFile(File(pdf.path)).defaultPage(curPage)
            }
            else -> {
                UiManager.showCenterShort(R.string.app_file_is_empty)
                finish()
                return
            }
        }
        if (password != null) configurator = configurator.password(password)
        paint = Paint()
        configurator.disableLongpress()
                .swipeHorizontal(Settings.isSwipeHorizontal())
                .nightMode(isNightMode)
                .pageFling(Settings.isSwipeHorizontal())
                .pageSnap(Settings.isSwipeHorizontal())
                .enableDoubletap(false)
                .fitEachPage(true) //                .spacing(ConvertUtils.dp2px(4))
                .onError { throwable: Throwable ->
                    LogUtils.e(throwable.message)
                    if (throwable is PdfPasswordException) {
                        if (!StringUtils.isEmpty(password)) {
                            UiManager.showCenterShort(R.string.app_password_error)
                        }
                        showInputDialog()
                    } else {
                        showAlertDialog()
                    }
                }
                .onPageError { page: Int, throwable: Throwable ->
                    LogUtils.e(throwable.message)
//                    UiManager.showShort(getString(R.string.app_cur_page) + page + getString(R.string.app_parse_error))
                    UiManager.showShort(getString(R.string.app_cur_page_parse_error, page))
                }
                .onDrawAll { canvas: Canvas?, pageWidth: Float, _: Float, displayedPage: Int ->
                    this.canvas = canvas
                    this.pageWidth = pageWidth
                    if (bookmarkMap.containsKey(displayedPage.toLong())) {
                        drawBookmark(canvas, pageWidth)
                    }
                }
                .onPageChange { page: Int, _: Int ->
                    curPage = page
                    app_tv_bookmark.isSelected = bookmarkMap.containsKey(page.toLong()) // 如果是书签则标红
                    app_quickbar_title.text = getTitle(page)
                    app_tv_pageinfo2.text = (page + 1).toString() + " / " + this.pageCount
                    app_tv_pageinfo.text = (page + 1).toString() + " / " + this.pageCount
                    app_sb_progress.progress = page
                }
                .onLoad {
                    pageCount = app_pdfview.pageCount
                    app_sb_progress.max = pageCount - 1
                    val list = app_pdfview.tableOfContents
                    findContent(list)
                    val fragmentList = supportFragmentManager.fragments
                    for (f in fragmentList) {
                        if (f is IContentFragInterface) {
                            contentFragInterface = f
                        } else if (f is IBkFragInterface) {
                            bkFragInterface = f
                        }
                    }
                    contentFragInterface?.update(list)
                    bkFragInterface?.update(bookmarkMap.values)
                    val keySet: Set<Long> = contentMap.keys
                    pageList.addAll(keySet)
                    pageList.sort()
                    if (!Settings.isNightMode()) {
                        app_pdfview_bg.background = ColorDrawable(Color.WHITE)
                        if (Settings.isSwipeHorizontal()) {
                            val page = (app_pdfview.pageCount / 2.toFloat()).roundToInt()
                            val sizeF = app_pdfview.getPageSize(page)
                            val lp = app_pdfview_bg.layoutParams
                            lp.width = sizeF.width.toInt()
                            lp.height = sizeF.height.toInt()
                            app_pdfview_bg.layoutParams = lp
                        } else {
                            val lp = app_pdfview_bg.layoutParams
                            lp.width = ScreenUtils.getScreenWidth()
                            lp.height = ScreenUtils.getScreenHeight()
                            app_pdfview_bg.layoutParams = lp
                        }
                    } else {
                        app_pdfview_bg.background = ColorDrawable(Color.BLACK)
                    }
                }
                .onTap { event: MotionEvent ->
                    if (autoDisp?.isDisposed == false) {
                        isPause = if (toolbar?.alpha == 1.0f) {
                            hideBar()
                            enterFullScreen()
                            false
                        } else {
                            !isPause
                        }
                        return@onTap true
                    }
                    if (app_ll_read_method.translationY != ScreenUtils.getScreenHeight().toFloat()) {
                        closeReadMethod()
                        return@onTap true
                    }
                    if (app_ll_more.translationY != ScreenUtils.getScreenHeight().toFloat()) {
                        closeMore()
                        return@onTap true
                    }
                    val x = event.rawX
                    val previous: Float
                    val next: Float
                    if (Settings.isClickFlipPage()) {
                        previous = ScreenUtils.getScreenWidth() * 0.3f
                        next = ScreenUtils.getScreenWidth() * 0.7f
                    } else {
                        previous = 0f
                        next = ScreenUtils.getScreenWidth().toFloat()
                    }
                    if (ScreenUtils.isPortrait() && x <= previous) {
                        if (toolbar?.alpha == 1.0f) {
                            hideBar()
                            enterFullScreen()
                        } else {
                            var currentPage = app_pdfview.currentPage
                            app_pdfview.jumpTo(--currentPage, true)
                        }
                    } else if (ScreenUtils.isPortrait() && x >= next) {
                        if (toolbar?.alpha == 1.0f) {
                            hideBar()
                            enterFullScreen()
                        } else {
                            var currentPage = app_pdfview.currentPage
                            app_pdfview.jumpTo(++currentPage, true)
                        }
                    } else {
                        val visible = (toolbar?.alpha == 1.0f
                                && app_ll_bottombar.alpha == 1.0f)
                        if (visible) {
                            hideBar()
                            enterFullScreen()
                        } else {
                            exitFullScreen()
                            showBar()
                        }
                    }
                    true
                }
                .load()
    }

    private fun showInputDialog() {
        inputDialog.show()
    }

    private fun showAlertDialog() {
        alertDialog.show()
    }

    private fun drawBookmark(canvas: Canvas?, pageWidth: Float) {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.app_img_bookmark)
        val left = pageWidth - ConvertUtils.dp2px(36f)
        canvas?.drawBitmap(bitmap, left, 0f, paint)
    }

    private fun showQuickbar() {
        app_ll_undoredobar.animate()
                .setDuration(50)
                .alpha(1f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        app_ll_undoredobar.visibility = View.VISIBLE
                    }
                })
                .start()
    }

    private fun openContent() {
        app_ll_content.animate()
                .setDuration(250)
                .setStartDelay(100)
                .translationX(0f)
                .setUpdateListener { valueAnimator: ValueAnimator -> app_screen_cover.alpha = valueAnimator.animatedFraction }
                .start()
    }

    private fun closeContent(listener: Animator.AnimatorListener?) {
        app_ll_content.animate()
                .setDuration(250)
                .translationX(-app_ll_content.measuredWidth.toFloat())
                .setUpdateListener { valueAnimator: ValueAnimator -> app_screen_cover.alpha = 1 - valueAnimator.animatedFraction }
                .setListener(listener)
                .start()
    }

    private fun findContent(list: List<PdfDocument.Bookmark>) {
        for (bk in list) {
            contentMap[bk.pageIdx] = bk
            if (bk.hasChildren()) {
                findContent(bk.children)
            }
        }
    }

    private fun getTitle(page: Int): String? {
        var page = page
        if (contentMap.isEmpty()) return getString(R.string.app_have_no_content)
        var title: String?
        val bk = contentMap[page.toLong()]
        title = bk?.title
        if (StringUtils.isEmpty(title)) {
            if (page < pageList[0]) {
                title = contentMap[pageList[0]]?.title
            } else {
                var index = pageList.indexOf(page.toLong())
                while (index == -1) {
                    index = pageList.indexOf(page--.toLong())
                }
                title = contentMap[pageList[index]]?.title
            }
        }
        return title
    }

    private fun showBar() {
        hideBar = false
        toolbar?.animate()?.setDuration(250)?.alpha(1f)
                ?.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        toolbar?.visibility = View.VISIBLE
                    }
                })?.start()
        app_ll_bottombar.animate().setDuration(250).alpha(1f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        app_ll_bottombar.visibility = View.VISIBLE
                    }
                }).start()
        app_tv_pageinfo.animate().setDuration(250).alpha(1f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        app_tv_pageinfo.visibility = View.VISIBLE
                    }
                }).start()
    }

    private fun hideBar() {
        hideBar = true
        toolbar?.animate()?.setDuration(250)?.alpha(0f)
                ?.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        toolbar?.visibility = View.GONE
                    }
                })?.start()
        app_ll_bottombar.animate().setDuration(250).alpha(0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (app_ll_bottombar != null) app_ll_bottombar.visibility = View.GONE
                    }
                }).start()
        app_tv_pageinfo.animate().setDuration(250).alpha(0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (app_tv_pageinfo != null) app_tv_pageinfo.visibility = View.GONE
                    }
                }).start()
        app_ll_undoredobar.animate().setDuration(250).alpha(0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (app_ll_undoredobar != null) app_ll_undoredobar.visibility = View.GONE
                    }
                }).start()
        if (app_ibtn_quickbar_action != null) app_ibtn_quickbar_action.isSelected = false // 初始化为 Undo 状态
    }

    private fun enterFullScreen() {
        if (Settings.isShowStatusBar()) {
            UiManager.setTranslucentStatusBar(this)
            app_ll_content.setPadding(0, ConvertUtils.dp2px(25f), 0, 0)
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        } else {
            UiManager.setTransparentStatusBar(this)
            app_ll_content.setPadding(0, 0, 0, 0)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun exitFullScreen() {
        if (Settings.isShowStatusBar()) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
        toolbar?.post { UiManager.setNavigationBarColor(this, resources.getColor(R.color.base_black)) }
    }

    companion object {
        private const val EXTRA_PDF = "EXTRA_PDF"
        private const val BUNDLE_CUR_PAGE = "BUNDLE_CUR_PAGE"
        private const val BUNDLE_PASSWORD = "BUNDLE_PASSWORD"
        private const val REQUEST_CODE_SETTINGS = 101
        private const val OFFSET_Y = -0.5f // 自动滚动的偏离值
        /**
         * 非外部文件打开
         */
        fun start(context: Context, pdf: PDF?) {
            val starter = Intent(context, PreviewActivity::class.java)
            starter.putExtra(EXTRA_PDF, pdf)
            context.startActivity(starter)
        }
    }
}