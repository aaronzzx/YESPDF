package com.aaron.yespdf.main;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.impl.TextWatcherImpl;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.DataManager;
import com.aaron.yespdf.common.DialogManager;
import com.aaron.yespdf.common.GroupingAdapter;
import com.aaron.yespdf.common.UiManager;
import com.aaron.yespdf.common.XGridDecoration;
import com.aaron.yespdf.common.YGridDecoration;
import com.aaron.yespdf.common.bean.Collection;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.event.AllEvent;
import com.aaron.yespdf.common.utils.DialogUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.github.mmin18.widget.RealtimeBlurView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class CollectionFragment extends DialogFragment implements IOperation, AbstractAdapter.ICommInterface<PDF>, GroupingAdapter.Callback {

    private static final String BUNDLE_NAME = "BUNDLE_NAME";

    @BindView(R2.id.app_blur_view)
    RealtimeBlurView realtimeBlurView;
    @BindView(R2.id.app_vg_operation)
    ViewGroup vgOperationBar;
    @BindView(R2.id.app_tv_regrouping)
    TextView tvRegrouping;
    @BindView(R2.id.app_ibtn_cancel)
    ImageButton ibtnCancel;
    @BindView(R2.id.app_tv_title)
    TextView tvTitle;
    @BindView(R2.id.app_ibtn_delete)
    ImageButton ibtnDelete;
    @BindView(R2.id.app_ibtn_select_all)
    ImageButton ibtnSelectAll;
    @BindView(R2.id.app_et_name)
    EditText etName;
    @BindView(R2.id.app_ibtn_clear)
    ImageButton ibtnClear;
    @BindView(R2.id.app_rv_collection)
    RecyclerView rvCollection;

    private Unbinder unbinder;
    private AbstractAdapter adapter;
    private TextView tvDeleteDescription;
    private EditText etInput;
    private BottomSheetDialog deleteDialog;
    private BottomSheetDialog regroupingDialog;
    private Dialog addNewGroupDialog;

    private String name;
    private String newDirName;
    private List<PDF> pdfList = new ArrayList<>();
    private List<PDF> selectPDFList;
    private List<Collection> savedCollections = DataManager.getCollectionList();

    static CollectionFragment newInstance(String name) {
        CollectionFragment fragment = new CollectionFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.AppCollectionFragment);
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.dimAmount = 0.0F;
        lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setWindowAnimations(R.style.AppPwCollection);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
//        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_fragment_collection, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 监听返回键
        View view = getView();
        if (view != null) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                        if (vgOperationBar.getVisibility() == View.VISIBLE) {
                            cancelSelect();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStartOperation() {
        etName.setEnabled(false);
        cancelRename();
        tvTitle.setText(getString(R.string.app_selected_zero));
        ibtnSelectAll.setSelected(false);
        OperationBarHelper.show(vgOperationBar);
        tvRegrouping.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSelect(List<PDF> list, boolean selectAll) {
        LogUtils.e(list);
        ibtnDelete.setEnabled(list.size() > 0);
        ibtnSelectAll.setSelected(selectAll);
        tvTitle.setText(getString(R.string.app_selected) + "(" + list.size() + ")");
        selectPDFList = list;
    }

    @Override
    public void delete() {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<List<String>>() {
            @Override
            public List<String> doInBackground() {
                pdfList.removeAll(selectPDFList);
                if (pdfList.isEmpty()) DBHelper.deleteCollection(name);
                return DBHelper.deletePDF(selectPDFList);
            }

            @Override
            public void onSuccess(List<String> nameList) {
                DataManager.updateAll();
                UiManager.showShort(R.string.app_delete_completed);
                cancelSelect();
                adapter.notifyDataSetChanged();
                EventBus.getDefault().post(new PdfDeleteEvent(nameList, name, pdfList.isEmpty()));
                if (pdfList.isEmpty()) dismiss();
            }
        });
    }

    @Override
    public void selectAll(boolean selectAll) {
        ibtnSelectAll.setSelected(selectAll);
        adapter.selectAll(selectAll);
    }

    @Override
    public void cancelSelect() {
        etName.setEnabled(true);
        OperationBarHelper.hide(vgOperationBar);
        adapter.cancelSelect();
        tvRegrouping.setVisibility(View.GONE);
        if (regroupingDialog != null && regroupingDialog.isShowing()) {
            regroupingDialog.dismiss();
        }
    }

    /**
     * 分组方法，属于 GroupingAdapter
     */
    @Override
    public void onAddNewGroup() {
        if (addNewGroupDialog == null) {
            addNewGroupDialog = DialogManager.createInputDialog(getActivity(), new DialogManager.InputDialogCallback() {
                @Override
                public void onTitle(TextView tv) {
                    tv.setText(R.string.app_add_new_group);
                }

                @Override
                public void onInput(EditText et) {
                    etInput = et;
                    et.setInputType(InputType.TYPE_CLASS_TEXT);
                    et.setHint(R.string.app_type_new_group_name);
                }

                @Override
                public void onLeft(Button btn) {
                    btn.setText(R.string.app_cancel);
                    btn.setOnClickListener(v -> addNewGroupDialog.dismiss());
                }

                @Override
                public void onRight(Button btn) {
                    btn.setText(R.string.app_confirm);
                    btn.setOnClickListener(v -> createNewGroup(etInput.getText().toString()));
                }
            });
            addNewGroupDialog.setOnDismissListener(dialog -> etInput.setText(""));
        }
        addNewGroupDialog.show();
    }

    private void createNewGroup(String name) {
        if (StringUtils.isEmpty(name)) {
            UiManager.showShort(R.string.app_type_new_group_name);
            return;
        }
        for (Collection c : savedCollections) {
            if (c.getName().equals(name)) {
                UiManager.showShort(R.string.app_group_name_existed);
                return;
            }
        }
        pdfList.removeAll(selectPDFList);
        DataManager.updatePDFs();
        DBHelper.insertNewCollection(name, selectPDFList);
        cancelSelect();
        addNewGroupDialog.dismiss();
        notifyGroupUpdate();
    }

    /**
     * 分组方法，属于 GroupingAdapter
     */
    @Override
    public void onAddToGroup(String dir) {
        if (name.equals(dir)) {
            cancelSelect();
            return;
        }
        pdfList.removeAll(selectPDFList);
        DBHelper.insertPDFsToCollection(dir, selectPDFList);
        DataManager.updatePDFs();
        cancelSelect();
        notifyGroupUpdate();
    }

    private void notifyGroupUpdate() {
        if (pdfList.isEmpty()) {
            dismiss();
        } else {
            adapter.notifyDataSetChanged();
        }
        EventBus.getDefault().post(new AllEvent(pdfList.isEmpty(), name));
    }

    @Override
    public String deleteDescription() {
        return null;
    }

    private void initView() {
        Bundle args = getArguments();

        if (args != null) {
            name = args.getString(BUNDLE_NAME);
            etName.setText(name);
            pdfList.addAll(DataManager.getPdfList(name));
        }

        createDeleteDialog();
        setListener();

        rvCollection.addItemDecoration(new XGridDecoration());
        rvCollection.addItemDecoration(new YGridDecoration());
        GridLayoutManager lm = new GridLayoutManager(getActivity(), 3);
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (pdfList.isEmpty()) {
                    return 3;
                }
                return 1;
            }
        });
        rvCollection.setLayoutManager(lm);

        adapter = new CollectionAdapter(this, pdfList);
        rvCollection.setAdapter(adapter);
    }

    private void createDeleteDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.app_bottomdialog_delete, null);
        tvDeleteDescription = view.findViewById(R.id.app_tv_description);
        Button btnCancel = view.findViewById(R.id.app_btn_cancel);
        Button btnDelete = view.findViewById(R.id.app_btn_delete);
        btnCancel.setOnClickListener(v -> deleteDialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            deleteDialog.dismiss();
            delete();
        });
        deleteDialog = DialogUtils.createBottomSheetDialog(getActivity(), view);
    }

    @SuppressLint("SetTextI18n")
    private void setListener() {
        realtimeBlurView.setOnClickListener(v -> {
            if (etName.hasFocus()) {
                cancelRename();
            } else if (vgOperationBar.getVisibility() == View.VISIBLE) {
                cancelSelect();
            } else {
                dismiss();
            }
        });
        tvRegrouping.setOnClickListener(v -> {
            if (regroupingDialog == null) {
                regroupingDialog = DialogManager.createGroupingDialog(getActivity(), true, this);
            }
            regroupingDialog.show();
        });
        ibtnCancel.setOnClickListener(v -> cancelSelect());
        ibtnDelete.setOnClickListener(v -> {
            tvDeleteDescription.setText(getString(R.string.app_will_delete) + " " + selectPDFList.size() + " " + getString(R.string.app_delete_for_collection));
            deleteDialog.show();
        });
        ibtnSelectAll.setOnClickListener(v -> selectAll(!v.isSelected()));
        etName.setOnFocusChangeListener((v, hasFocus) -> {
            ibtnClear.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
        });
        etName.addTextChangedListener(new TextWatcherImpl() {
            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {
                newDirName = c.toString().trim();
            }
        });
        etName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                cancelRename();
                return true;
            }
        });
        ibtnClear.setOnClickListener(v -> {
            etName.setText(" ");
            etName.setSelection(0);
        });
    }

    private void cancelRename() {
        KeyboardUtils.hideSoftInput(etName);
        etName.clearFocus();
        if (newDirName != null && StringUtils.isEmpty(newDirName)) {
            etName.setText(name);
            UiManager.showShort(R.string.app_not_support_empty_string);
        } else {
            boolean success = DBHelper.updateDirName(name, newDirName);
            if (success) EventBus.getDefault().post(new AllEvent());
        }
    }
}
