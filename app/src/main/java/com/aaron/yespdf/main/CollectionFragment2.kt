package com.aaron.yespdf.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aaron.base.impl.TextWatcherImpl
import com.aaron.yespdf.R
import com.aaron.yespdf.common.*
import com.aaron.yespdf.common.bean.PDF
import com.aaron.yespdf.common.bean.Shortcut
import com.aaron.yespdf.common.event.AllEvent
import com.aaron.yespdf.common.utils.DialogUtils
import com.aaron.yespdf.common.utils.GreyUI
import com.aaron.yespdf.common.utils.ShortcutUtils
import com.aaron.yespdf.preview.PreviewActivity
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.app_fragment_collection.*
import kotlinx.android.synthetic.main.app_include_operation_bar.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class CollectionFragment2 : DialogFragment(), IOperation, GroupingAdapter.Callback {

    private val deleteDialog: BottomSheetDialog by lazy(LazyThreadSafetyMode.NONE) {
        val view = LayoutInflater.from(activity!!).inflate(R.layout.app_bottomdialog_delete, null)
        tvDeleteDescription = view.findViewById(R.id.app_tv_description)
        deleteLocal = view.findViewById(R.id.app_delete_local)
        val btnCancel = view.findViewById<Button>(R.id.app_btn_cancel)
        val btnDelete = view.findViewById<Button>(R.id.app_btn_delete)
        val cb = deleteLocal?.findViewById<CheckBox>(R.id.app_cb)
        deleteLocal?.setOnClickListener {
            it.isSelected = !it.isSelected
            cb?.isChecked = it.isSelected
        }
        btnCancel.setOnClickListener { deleteDialog.dismiss() }
        btnDelete.setOnClickListener {
            deleteDialog.dismiss()
            delete(deleteLocal?.isSelected ?: false)
            deleteLocal?.isSelected = false
            cb?.isChecked = false
        }
        DialogUtils.createBottomSheetDialog(activity, view)
    }
    private val regroupingDialog: BottomSheetDialog by lazy(LazyThreadSafetyMode.NONE) {
        DialogManager.createGroupingDialog(activity!!, true, this)
    }
    private val addNewGroupDialog: Dialog by lazy(LazyThreadSafetyMode.NONE) {
        DialogManager.createInputDialog(activity!!) { tvTitle, etInput, btnLeft, btnRight ->
            tvTitle.setText(R.string.app_add_new_group)
            etInput.inputType = InputType.TYPE_CLASS_TEXT
            etInput.setHint(R.string.app_type_new_group_name)
            this.etInput = etInput
            btnLeft.setText(R.string.app_cancel)
            btnLeft.setOnClickListener { addNewGroupDialog.dismiss() }
            btnRight.setText(R.string.app_confirm)
            btnRight.setOnClickListener {
                createNewGroup(this.etInput?.text.toString())
            }
        }.apply { setOnDismissListener { etInput?.setText("") } }
    }

    private lateinit var adapter: CollectionAdapter2
    private var tvDeleteDescription: TextView? = null
    private var deleteLocal: View? = null
    private var etInput: EditText? = null
    private var name: String? = null
    private var newDirName: String? = null
    private val selectPDFList: MutableList<PDF> = ArrayList()
    private val pdfList: MutableList<PDF> = ArrayList()
    private val savedCollections = DataManager.getCollectionList()
    private var isNeedUpdateDb = false
    private var isHorizontalLayout = false
    private var xItemDecoration: RecyclerView.ItemDecoration? = null
    private var yItemDecoration: RecyclerView.ItemDecoration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppCollectionFragment)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.run {
            GreyUI.grey(this, Settings.globalGrey)
            val lp = this.attributes
            lp.gravity = Gravity.CENTER
            lp.dimAmount = 0.0f
            lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            this.attributes = lp
            this.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            this.setWindowAnimations(R.style.AppPwCollection)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
        if (isHorizontalLayout != Settings.linearLayout) {
            isHorizontalLayout = Settings.linearLayout
            initView()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.app_fragment_collection, container, false)
    }

    override fun localDeleteVisibility(): Int = View.VISIBLE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments
        if (args != null) {
            name = args.getString(BUNDLE_NAME)
            app_et_name.setText(name)
        }
        initView()
    }

    override fun onResume() {
        super.onResume()
        isNeedUpdateDb = false
        pdfList.apply {
            clear()
            addAll(DataManager.getPdfList(name))
            adapter.setNewData(pdfList)
        }
        // 监听返回键
        val view = view
        if (view != null) {
            view.isFocusableInTouchMode = true
            view.requestFocus()
            view.setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                    if (app_vg_operation.visibility == View.VISIBLE) {
                        cancelSelect()
                        return@OnKeyListener true
                    }
                }
                false
            })
        }
    }

    override fun onStop() {
        super.onStop()
        cancelSelect()
        if (isNeedUpdateDb) {
            DBHelper.updatePDFs(pdfList)
            DataManager.updatePDFs()
            LiveDataBus.with<Any>(EVENT_UPDATE_ALL_FRAGMENT).value = null
        }
    }

    private fun startPicking() {
        selectPDFList.clear()
        app_et_name.isEnabled = false
        cancelRename()
        app_ibtn_select_all.isSelected = false
        OperationBarHelper.show(app_vg_operation)
        app_tv_regrouping.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun select(selectAll: Boolean) {
        app_ibtn_create_shortcut.isEnabled = selectPDFList.isNotEmpty()
        app_ibtn_delete.isEnabled = selectPDFList.isNotEmpty()
        app_ibtn_select_all.isSelected = selectAll
        app_tv_operationbar_title.text = getString(R.string.app_selected_count, selectPDFList.size)
    }

    override fun createShortcut() {
    }

    override fun delete(deleteLocal: Boolean) {
        ThreadUtils.executeByIo<List<String>>(object : ThreadUtils.SimpleTask<List<String>>() {
            override fun doInBackground(): List<String>? {
                if (deleteLocal) {
                    selectPDFList.forEach { File(it.path).delete() }
                }
                selectPDFList.run { pdfList.removeAll(this) }
                if (pdfList.isEmpty()) DBHelper.deleteCollection(name)
                return DBHelper.deletePDF(selectPDFList)
            }

            override fun onSuccess(nameList: List<String>?) {
                DataManager.updateAll()
                UiManager.showShort(R.string.app_delete_completed)
                cancelSelect()
                adapter.notifyDataSetChanged()
                nameList?.run { EventBus.getDefault().post(PdfDeleteEvent(this, name!!, pdfList.isEmpty())) }
                if (pdfList.isEmpty()) dismiss()
            }
        })
    }

    override fun selectAll(selectAll: Boolean) {
        app_ibtn_select_all.isSelected = selectAll
        adapter.selectAll(selectAll)
        selectPDFList.clear()
        if (selectAll) {
            selectPDFList.addAll(pdfList)
        }
        select(selectAll)
    }

    override fun cancelSelect() {
        app_et_name.isEnabled = true
        OperationBarHelper.hide(app_vg_operation)
        adapter.exitSelectMode()
        app_tv_regrouping.visibility = View.GONE
        if (regroupingDialog.isShowing) {
            regroupingDialog.dismiss()
        }
    }

    /**
     * 分组方法，属于 GroupingAdapter
     */
    override fun onAddNewGroup() {
        addNewGroupDialog.show()
    }

    private fun createNewGroup(name: String) {
        if (StringUtils.isEmpty(name)) {
            UiManager.showShort(R.string.app_type_new_group_name)
            return
        }
        for (c in savedCollections) {
            if (c.name == name) {
                UiManager.showShort(R.string.app_group_name_existed)
                return
            }
        }
        pdfList.removeAll(selectPDFList)
        var max = selectPDFList.size - 1
        for (pdf in selectPDFList) {
            pdf.position = max--
        }
        DBHelper.insertNewCollection(name, selectPDFList)
        DataManager.updatePDFs()
        pdfList.removeAll(selectPDFList)

        cancelSelect()
        addNewGroupDialog.dismiss()
        notifyGroupUpdate()
    }

    /**
     * 分组方法，属于 GroupingAdapter
     */
    override fun onAddToGroup(dir: String) {
        if (name == dir) {
            cancelSelect()
            return
        }
        pdfList.removeAll(selectPDFList)
        val targetList = DataManager.getPdfList(dir)
        targetList.addAll(selectPDFList)
        var max = targetList.size - 1
        for (pdf in targetList) {
            pdf.position = max--
        }
        DBHelper.insertPDFsToCollection(dir, targetList)
        DataManager.updatePDFs()
        cancelSelect()
        notifyGroupUpdate()
    }

    private fun notifyGroupUpdate() {
//        if (pdfList.isEmpty()) {
//        } else {
//        }
        dismiss()
        adapter.notifyDataSetChanged()
        EventBus.getDefault().post(AllEvent(pdfList.isEmpty(), name))
    }

    override fun deleteDescription(): String? {
        return getString(R.string.app_whether_delete_all, selectPDFList.size)
    }

    private fun initView() {
        setListener()
        if (xItemDecoration != null) {
            app_rv_collection.removeItemDecoration(xItemDecoration!!)
        }
        if (yItemDecoration != null) {
            app_rv_collection.removeItemDecoration(yItemDecoration!!)
        }
        xItemDecoration = XGridDecoration()
        yItemDecoration = YGridDecoration()
        app_rv_collection.addItemDecoration(xItemDecoration!!)
        app_rv_collection.addItemDecoration(yItemDecoration!!)
        val lm = if (isHorizontalLayout) {
            LinearLayoutManager(activity)
        } else {
            GridLayoutManager(activity, 3).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (pdfList.isEmpty()) 3 else 1
                    }
                }
            }
        }
        app_rv_collection.layoutManager = lm
        adapter = CollectionAdapter2(pdfList)
        adapter.setOnItemClickListener { _, view, position ->
            if (adapter.isSelectMode) {
                val pdf = pdfList[position]
                val checkBox: CheckBox? = view.findViewById(adapter.checkBoxId)
                if (checkBox?.isChecked == true) {
                    selectPDFList.remove(pdf)
                } else {
                    selectPDFList.add(pdf)
                }
                select(selectPDFList.size == pdfList.size)
            } else {
                val pdf = pdfList[position]
                PreviewActivity.start(context!!, pdf)
//                dismissAllowingStateLoss()
            }
        }
        adapter.setOnItemDragListener(object : OnItemDragListener {
            private lateinit var checkBox: CheckBox
            private lateinit var pdf: PDF

            override fun onItemDragMoving(
                    viewHolder: RecyclerView.ViewHolder,
                    fromPos: Int,
                    target: RecyclerView.ViewHolder,
                    toPos: Int
            ) {
            }

            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder, position: Int) {
                pdf = pdfList[position]
                checkBox = viewHolder.itemView.findViewById(R.id.app_cb)
                checkBox.visibility = View.GONE
            }

            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder, position: Int) {
                if (!adapter.isSelectMode) {
                    startPicking()
                    selectPDFList.add(pdfList[position])
                    select(selectPDFList.size == pdfList.size)
                    adapter.enterSelectMode(viewHolder.itemView, position)
                }
                checkBox.visibility = View.VISIBLE

                var max = pdfList.size - 1
                for (pdf in pdfList) {
                    pdf.position = max--
                }
                isNeedUpdateDb = true
            }
        })
        val dragHelper = ItemTouchHelper(ItemDragAndSwipeCallback(adapter))
        dragHelper.attachToRecyclerView(app_rv_collection)
        adapter.enableDragItem(dragHelper)
        app_rv_collection.adapter = adapter
    }

    override fun showExport(): Boolean = true

    @SuppressLint("SetTextI18n")
    private fun setListener() {
        app_blur_view.setOnClickListener {
            when {
                app_et_name.hasFocus() -> {
                    cancelRename()
                }
                app_vg_operation.visibility == View.VISIBLE -> {
                    cancelSelect()
                }
                else -> {
                    dismissAllowingStateLoss()
                }
            }
        }
        app_tv_regrouping.setOnClickListener {
            regroupingDialog.show()
        }
        app_ibtn_cancel.setOnClickListener { cancelSelect() }
        app_ibtn_create_shortcut.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                visibility = View.VISIBLE
            }
            setOnClickListener {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    ToastUtils.showShort(R.string.app_not_support_launcher_shortcut)
                    return@setOnClickListener
                }
                val selecteds = selectPDFList
                if (selecteds.size > 1) {
                    ToastUtils.showShort(App.getContext().resources.getString(R.string.app_shortcut_max), 1)
                    return@setOnClickListener
                }
                selecteds[0].let {
                    ShortcutUtils.createPinnedShortcut(App.getContext(), Shortcut(
                            PreviewActivity::class.java,
                            it.id.toString(),
                            it.name,
                            it.name,
                            R.drawable.app_ic_shortcut_book,
                            PreviewActivity.EXTRA_PATH,
                            it.path
                    ))
                }
                if (Settings.firstCreateShortcut) {
                    ToastUtils.showLong(R.string.app_first_create_shortcut_tips)
                    Settings.firstCreateShortcut = false
                }
                cancelSelect()
            }
        }
        app_ibtn_delete.setOnClickListener {
            deleteDialog.show()
            tvDeleteDescription?.text = getString(R.string.app_whether_delete_all, selectPDFList.size)
            deleteLocal?.visibility = localDeleteVisibility()
        }
        app_ibtn_select_all.setOnClickListener { selectAll(!it.isSelected) }
        app_et_name.onFocusChangeListener = View.OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            app_ibtn_clear.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }
        app_et_name.addTextChangedListener(object : TextWatcherImpl() {
            override fun onTextChanged(c: CharSequence, i: Int, i1: Int, i2: Int) {
                newDirName = c.toString().trim { it <= ' ' }
            }
        })
        app_et_name.setOnKeyListener { _, _, _ ->
            cancelRename()
            true
        }
        app_ibtn_clear.setOnClickListener {
            app_et_name.setText(" ")
            app_et_name.setSelection(0)
        }
    }

    private fun cancelRename() {
        KeyboardUtils.hideSoftInput(app_et_name)
        app_et_name.clearFocus()
        if (newDirName != null && StringUtils.isEmpty(newDirName)) {
            app_et_name.setText(name)
            UiManager.showShort(R.string.app_not_support_empty_string)
        } else {
            val success = DBHelper.updateDirName(name ?: "", newDirName ?: "")
            if (success) EventBus.getDefault().post(AllEvent())
        }
    }

    companion object {
        const val EVENT_UPDATE_ALL_FRAGMENT = "EVENT_UPDATE_ALL_FRAGMENT"
        private const val BUNDLE_NAME = "BUNDLE_NAME"

        fun newInstance(name: String?): CollectionFragment2 {
            val fragment = CollectionFragment2()
            val args = Bundle()
            args.putString(BUNDLE_NAME, name)
            fragment.arguments = args
            return fragment
        }
    }
}