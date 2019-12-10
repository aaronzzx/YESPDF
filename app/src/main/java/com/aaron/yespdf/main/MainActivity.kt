package com.aaron.yespdf.main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.view.*
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.aaron.base.impl.OnClickListenerImpl
import com.aaron.yespdf.R
import com.aaron.yespdf.about.AboutActivity
import com.aaron.yespdf.common.*
import com.aaron.yespdf.common.event.HotfixEvent
import com.aaron.yespdf.common.event.ImportEvent
import com.aaron.yespdf.filepicker.SelectActivity
import com.aaron.yespdf.settings.SettingsActivity
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.PermissionUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.app_activity_main.*
import kotlinx.android.synthetic.main.app_include_operation_bar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class MainActivity : CommonActivity(), IMainView {

    private val presenter: IMainPresenter by lazy(LazyThreadSafetyMode.NONE) {
        MainPresenter(this)
    }

    private val pwMenu: PopupWindow by lazy(LazyThreadSafetyMode.NONE) {
        @SuppressLint("InflateParams")
        val pwView = LayoutInflater.from(this).inflate(R.layout.app_pw_main, null)
        val tvImport = pwView.findViewById<TextView>(R.id.app_tv_import)
        val tvSettings = pwView.findViewById<TextView>(R.id.app_tv_settings)
        val tvAbout = pwView.findViewById<TextView>(R.id.app_tv_about)
        PopupWindow(pwView).apply {
            tvImport.setOnClickListener {
                PermissionUtils.permission(PermissionConstants.STORAGE)
                        .callback(object : PermissionUtils.SimpleCallback {
                            override fun onGranted() {
                                SelectActivity.start(this@MainActivity, SELECT_REQUEST_CODE, DataManager.getPathList() as ArrayList<String?>)
                            }

                            override fun onDenied() {
                                UiManager.showShort(R.string.app_have_no_storage_permission)
                            }
                        })
                        .request()
                dismiss()
            }
            tvSettings.setOnClickListener {
                SettingsActivity.start(this@MainActivity)
                dismiss()
            }
            tvAbout.setOnClickListener {
                AboutActivity.start(this@MainActivity)
                dismiss()
            }
            animationStyle = R.style.AppPwMenu
            isFocusable = true
            isOutsideTouchable = true
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            elevation = ConvertUtils.dp2px(4f).toFloat()
        }
    }
    private val hotfixDialog: Dialog by lazy(LazyThreadSafetyMode.NONE) {
        DialogManager.createDoubleBtnDialog(this) { tvTitle, tvContent, btnLeft, btnRight ->
            tvTitle.setText(R.string.app_find_update)
            tvContent.setText(R.string.app_restart_to_update)
            btnLeft.setText(R.string.app_later)
            btnLeft.setOnClickListener(object : OnClickListenerImpl() {
                override fun onViewClick(v: View, interval: Long) {
                    hotfixDialog.dismiss()
                }
            })
            btnRight.setText(R.string.app_restart_right_now)
            btnRight.setOnClickListener(object : OnClickListenerImpl() {
                override fun onViewClick(v: View, interval: Long) {
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        Process.killProcess(Process.myPid())
                    }
                }
            })
        }
    }
    private val deleteDialog: BottomSheetDialog by lazy(LazyThreadSafetyMode.NONE) {
        DialogManager.createDeleteDialog(this) { tv, btnLeft, btnRight ->
            tvDeleteDescription = tv
            btnLeft.setOnClickListener { deleteDialog.dismiss() }
            btnRight.setOnClickListener {
                deleteDialog.dismiss()
                operation?.delete()
            }
        }
    }
    private val importInfoDialog: BottomSheetDialog by lazy(LazyThreadSafetyMode.NONE) {
        DialogManager.createImportInfoDialog(this) { tvTitle, progressBar, tvProgressInfo, btn ->
            tvDialogTitle = tvTitle
            pbDialogProgress = progressBar
            tvDialogProgressInfo = tvProgressInfo
            btn.setOnClickListener(object : OnClickListenerImpl() {
                override fun onViewClick(v: View, interval: Long) {
                    importInfoDialog.dismiss()
                    pbDialogProgress?.max = 0
                }
            })
        }
    }

    private var operation: IOperation? = null
    private var tvDeleteDescription: TextView? = null
    private var tvDialogTitle: TextView? = null
    private var pbDialogProgress: ProgressBar? = null
    private var tvDialogProgressInfo: TextView? = null

    private var receiveHotfix = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val pathList = data?.getStringArrayListExtra(SelectActivity.EXTRA_SELECTED)
            val type = data?.getIntExtra(SelectActivity.EXTRA_TYPE, 0)
            val groupName = data?.getStringExtra(SelectActivity.EXTRA_GROUP_NAME)
            presenter.insertPDF(pathList, type, groupName)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.app_search) {
            SearchActivity.start(this)
        } else if (itemId == R.id.app_more) {
            val x = ConvertUtils.dp2px(6f)
            val y = ConvertUtils.dp2px(80f)
            pwMenu.showAtLocation(window.decorView, Gravity.TOP or Gravity.END, x, y)
        }
        return true
    }

    override fun onBackPressed() {
        if (app_vg_operation.visibility == View.VISIBLE) {
            finishOperation()
        } else {
            super.onBackPressed()
            if (receiveHotfix) {
                Process.killProcess(Process.myPid())
            }
        }
    }

    override fun layoutId(): Int = R.layout.app_activity_main

    override fun createToolbar(): Toolbar? = findViewById(R.id.app_toolbar)

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onHotfixSuccess(event: HotfixEvent) {
        receiveHotfix = true
        hotfixDialog.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onImportEvent(event: ImportEvent) {
        if (!importInfoDialog.isShowing) {
            event.stop = true
            return
        }
        tvDialogTitle?.text = getString(R.string.app_importing, event.name)
        if (pbDialogProgress?.max == 0) {
            pbDialogProgress?.max = event.totalProgress
        }
        pbDialogProgress?.progress = event.curProgress
        tvDialogProgressInfo?.text = getString(R.string.app_import_progress, event.curProgress, event.totalProgress)
    }

    fun injectOperation(fragment: IOperation) {
        operation = fragment
    }

    fun startOperation() {
        app_vp.setScrollable(false)
        app_ibtn_select_all.isSelected = false
        OperationBarHelper.show(app_vg_operation)
    }

    fun finishOperation() {
        app_vp.setScrollable(true)
        OperationBarHelper.hide(app_vg_operation)
        operation?.cancelSelect()
    }

    fun selectResult(count: Int, selectAll: Boolean) {
        app_ibtn_delete.isEnabled = count > 0
        app_ibtn_select_all.isSelected = selectAll
        app_tv_operationbar_title.text = getString(R.string.app_selected_count, count)
    }

    override fun onShowMessage(stringId: Int): Unit = UiManager.showShort(stringId)

    override fun onShowLoading(): Unit = importInfoDialog.show()

    override fun onHideLoading() {
        importInfoDialog.dismiss()
        pbDialogProgress?.max = 0
    }

    override fun onUpdate() {
        for (fragment in supportFragmentManager.fragments) {
            (fragment as? AllFragment2)?.run {
                update()
                return
            }
        }
    }

    private fun initView() {
        app_ibtn_cancel.setOnClickListener { finishOperation() }
        app_ibtn_delete.setOnClickListener {
            deleteDialog.show()
            tvDeleteDescription?.text = operation?.deleteDescription()
        }
        app_ibtn_select_all.setOnClickListener { operation?.selectAll(!it.isSelected) }

        app_tab_layout.setupWithViewPager(app_vp)
        app_vp.adapter = MainFragmentAdapter(supportFragmentManager)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    companion object {
        private const val SELECT_REQUEST_CODE = 101
    }
}