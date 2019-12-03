package com.aaron.yespdf.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.View.OnFocusChangeListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.aaron.base.impl.TextWatcherImpl
import com.aaron.yespdf.R
import com.aaron.yespdf.common.*
import com.aaron.yespdf.common.bean.PDF
import com.aaron.yespdf.common.event.AllEvent
import com.aaron.yespdf.common.utils.DialogUtils
import com.aaron.yespdf.main.AbstractAdapter.ICommInterface
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ThreadUtils.SimpleTask
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.app_fragment_collection.*
import kotlinx.android.synthetic.main.app_include_operation_bar.*
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class CollectionFragment : DialogFragment(), IOperation, ICommInterface<PDF>, GroupingAdapter.Callback {

    private val deleteDialog: BottomSheetDialog by lazy(LazyThreadSafetyMode.NONE) {
        val view = LayoutInflater.from(activity).inflate(R.layout.app_bottomdialog_delete, null)
        tvDeleteDescription = view.findViewById(R.id.app_tv_description)
        val btnCancel = view.findViewById<Button>(R.id.app_btn_cancel)
        val btnDelete = view.findViewById<Button>(R.id.app_btn_delete)
        btnCancel.setOnClickListener { deleteDialog.dismiss() }
        btnDelete.setOnClickListener {
            deleteDialog.dismiss()
            delete()
        }
        DialogUtils.createBottomSheetDialog(activity, view)
    }
    private val regroupingDialog: BottomSheetDialog by lazy(LazyThreadSafetyMode.NONE) {
        DialogManager.createGroupingDialog(activity, true, this)
    }
    private val addNewGroupDialog: Dialog by lazy(LazyThreadSafetyMode.NONE) {
        val temp = DialogManager.createInputDialog(activity, object : DialogManager.InputDialogCallback {
            override fun onTitle(tv: TextView) {
                tv.setText(R.string.app_add_new_group)
            }

            override fun onInput(et: EditText) {
                etInput = et
                et.inputType = InputType.TYPE_CLASS_TEXT
                et.setHint(R.string.app_type_new_group_name)
            }

            override fun onLeft(btn: Button) {
                btn.setText(R.string.app_cancel)
                btn.setOnClickListener { addNewGroupDialog.dismiss() }
            }

            override fun onRight(btn: Button) {
                btn.setText(R.string.app_confirm)
                btn.setOnClickListener { createNewGroup(etInput?.text.toString()) }
            }
        })
        temp.setOnDismissListener { etInput?.setText("") }
        temp
    }

    private var adapter: AbstractAdapter<*>? = null
    private var tvDeleteDescription: TextView? = null
    private var etInput: EditText? = null
    private var name: String? = null
    private var newDirName: String? = null
    private var selectPDFList: List<PDF>? = null
    private val pdfList: MutableList<PDF> = ArrayList()
    private val savedCollections = DataManager.getCollectionList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppCollectionFragment)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.run {
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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.app_fragment_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
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

    override fun onStartOperation() {
        app_et_name.isEnabled = false
        cancelRename()
        app_tv_title.text = getString(R.string.app_selected_zero)
        app_ibtn_select_all.isSelected = false
        OperationBarHelper.show(app_vg_operation)
        app_tv_regrouping.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    override fun onSelect(list: List<PDF>, selectAll: Boolean) {
        LogUtils.e(list)
        app_ibtn_delete.isEnabled = list.isNotEmpty()
        app_ibtn_select_all.isSelected = selectAll
        app_tv_title.text = getString(R.string.app_selected) + "(" + list.size + ")"
        selectPDFList = list
    }

    override fun delete() {
        ThreadUtils.executeByIo<List<String>>(object : SimpleTask<List<String>>() {
            override fun doInBackground(): List<String>? {
                selectPDFList?.run { pdfList.removeAll(this) }
                if (pdfList.isEmpty()) DBHelper.deleteCollection(name)
                return DBHelper.deletePDF(selectPDFList)
            }

            override fun onSuccess(nameList: List<String>?) {
                DataManager.updateAll()
                UiManager.showShort(R.string.app_delete_completed)
                cancelSelect()
                adapter!!.notifyDataSetChanged()
                nameList?.run { EventBus.getDefault().post(PdfDeleteEvent(this, name, pdfList.isEmpty())) }
                if (pdfList.isEmpty()) dismiss()
            }
        })
    }

    override fun selectAll(selectAll: Boolean) {
        app_ibtn_select_all.isSelected = selectAll
        adapter?.selectAll(selectAll)
    }

    override fun cancelSelect() {
        app_et_name.isEnabled = true
        OperationBarHelper.hide(app_vg_operation)
        adapter?.cancelSelect()
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
        selectPDFList?.run { pdfList.removeAll(this) }
        DataManager.updatePDFs()
        DBHelper.insertNewCollection(name, selectPDFList)
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
        selectPDFList?.run { pdfList.removeAll(this) }
        DBHelper.insertPDFsToCollection(dir, selectPDFList)
        DataManager.updatePDFs()
        cancelSelect()
        notifyGroupUpdate()
    }

    private fun notifyGroupUpdate() {
        if (pdfList.isEmpty()) {
            dismiss()
        } else {
            adapter?.notifyDataSetChanged()
        }
        EventBus.getDefault().post(AllEvent(pdfList.isEmpty(), name))
    }

    override fun deleteDescription(): String? {
        return null
    }

    private fun initView() {
        val args = arguments
        if (args != null) {
            name = args.getString(BUNDLE_NAME)
            app_et_name.setText(name)
            pdfList.addAll(DataManager.getPdfList(name))
        }
        setListener()
        app_rv_collection.addItemDecoration(XGridDecoration())
        app_rv_collection.addItemDecoration(YGridDecoration())
        val lm = GridLayoutManager(activity, 3)
        lm.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (pdfList.isEmpty()) {
                    3
                } else 1
            }
        }
        app_rv_collection.layoutManager = lm
        adapter = CollectionAdapter(this, pdfList)
        app_rv_collection.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun setListener() {
        app_vg_operation.setOnClickListener { }
        app_blur_view.setOnClickListener {
            when {
                app_et_name.hasFocus() -> {
                    cancelRename()
                }
                app_vg_operation.visibility == View.VISIBLE -> {
                    cancelSelect()
                }
                else -> {
                    dismiss()
                }
            }
        }
        app_tv_regrouping.setOnClickListener {
            regroupingDialog.show()
        }
        app_ibtn_cancel.setOnClickListener { cancelSelect() }
        app_ibtn_delete.setOnClickListener {
            tvDeleteDescription?.text = getString(R.string.app_will_delete) + " " + selectPDFList?.size + " " + getString(R.string.app_delete_for_collection)
            deleteDialog.show()
        }
        app_ibtn_select_all.setOnClickListener { selectAll(!it.isSelected) }
        app_et_name.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
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
            val success = DBHelper.updateDirName(name, newDirName)
            if (success) EventBus.getDefault().post(AllEvent())
        }
    }

    companion object {
        private const val BUNDLE_NAME = "BUNDLE_NAME"
        fun newInstance(name: String?): CollectionFragment {
            val fragment = CollectionFragment()
            val args = Bundle()
            args.putString(BUNDLE_NAME, name)
            fragment.arguments = args
            return fragment
        }
    }
}