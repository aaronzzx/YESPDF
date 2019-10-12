package com.aaron.yespdf.filepicker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.base.BaseFragment;
import com.aaron.base.impl.OnClickListenerImpl;
import com.aaron.base.impl.TextWatcherImpl;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.DialogManager;
import com.aaron.yespdf.common.GroupingAdapter;
import com.aaron.yespdf.common.UiManager;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewAllFragment extends BaseFragment implements IViewAllContract.V, ViewAllAdapter.Callback {

    @BindView(R2.id.app_horizontal_sv)
    HorizontalScrollView horizontalSv;
    @BindView(R2.id.app_ll)
    ViewGroup vgPath;
    @BindView(R2.id.app_tv_path)
    TextView tvPath;
    @BindView(R2.id.app_rv_select)
    RecyclerView rvSelect;
    @BindView(R2.id.app_btn_import_count)
    Button btnImportCount;
    ViewAllAdapter adapter;

    private IViewAllContract.P presenter;
    private Unbinder unbinder;
    private SelectActivity activity;
    private BottomSheetDialog importDialog;
    private BottomSheetDialog groupingDialog;

    private String newGroupName;
    private List<File> fileList = new ArrayList<>();
    List<String> selectList = new ArrayList<>();
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String path = (String) v.getTag();
            int index = vgPath.indexOfChild(v);
            int count = vgPath.getChildCount();
            vgPath.removeViews(index + 1, count - index - 1);
            presenter.listFile(path);
        }
    };
    private RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            selectList.clear();
            activity.ibtnSelectAll.setSelected(false);
            btnImportCount.setText(R.string.app_import_count);
            boolean enableSelectAll = adapter.reset();
            activity.ibtnSelectAll.setEnabled(enableSelectAll);
        }
    };

    static Fragment newInstance() {
        return new ViewAllFragment();
    }

    public ViewAllFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activity = (SelectActivity) getActivity();
        attachP();
        View view = inflater.inflate(R.layout.app_fragment_view_all, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        presenter.listStorage();
        return view;
    }

    void setFocus() {
        horizontalSv.requestFocus();
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.ibtnSelectAll.setVisibility(View.VISIBLE);
        activity.ibtnSearch.setVisibility(View.VISIBLE);
        activity.setRevealParam();

        horizontalSv.setFocusableInTouchMode(true);
        horizontalSv.requestFocus();
        horizontalSv.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (!presenter.canFinish()) {
                        presenter.goBack();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.unregisterAdapterDataObserver(dataObserver);
        unbinder.unbind();
        presenter.detachV();
    }

    @Override
    public void attachP() {
        presenter = new ViewAllPresenter(this);
    }

    @Override
    public void onShowMessage(int stringId) {
        UiManager.showShort(stringId);
    }

    @Override
    public void onShowFileList(List<File> fileList) {
        this.fileList.clear();
        this.fileList.addAll(fileList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onShowPath(List<String> pathList) {
        vgPath.removeViews(1, vgPath.getChildCount() - 1);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        StringBuilder parent = new StringBuilder(IViewAllContract.P.ROOT_PATH);
        for (String dirName : pathList) {
            TextView tvPath = (TextView) inflater.inflate(R.layout.app_include_tv_path, null);
            tvPath.setOnClickListener(onClickListener);
            tvPath.setText(dirName);
            parent.append("/").append(dirName); // 当前节点生成后即变成下一节点的父节点
            tvPath.setTag(parent.toString());
            vgPath.addView(tvPath);
        }
    }

    @Override
    public void onDirTap(String dirPath) {
        presenter.listFile(dirPath);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSelectResult(List<String> pathList, int total) {
        LogUtils.e(pathList.size());
        if (total != 0) {
            activity.ibtnSelectAll.setSelected(pathList.size() == total);
        }
        btnImportCount.setText(getString(R.string.app_import) + "(" + pathList.size() + ")");
        selectList.clear();
        selectList.addAll(pathList);
    }

    private void initView() {
        tvPath.setOnClickListener(onClickListener);
        tvPath.setTag(IViewAllContract.P.ROOT_PATH); // 原始路径
        btnImportCount.setOnClickListener(new OnClickListenerImpl() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onViewClick(View v, long interval) {
                if (KeyboardUtils.isSoftInputVisible(activity)) {
                    KeyboardUtils.hideSoftInput(activity);
                }
                if (selectList.isEmpty()) {
                    UiManager.showShort(R.string.app_have_not_select);
                } else {
                    if (importDialog == null) {
                        initImportDialog();
                    }
                    importDialog.show();
                }
            }
        });
        activity.ibtnSelectAll.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            adapter.selectAll(v.isSelected());
        });

        activity.ibtnSelectAll.setEnabled(false); // XML 设置无效，只能这里初始化
        RecyclerView.LayoutManager lm = new LinearLayoutManager(activity);
        rvSelect.setLayoutManager(lm);
        adapter = new ViewAllAdapter(fileList, activity.importeds, this);
        adapter.registerAdapterDataObserver(dataObserver);
        rvSelect.setAdapter(adapter);
    }

    private void initImportDialog() {
        importDialog = DialogManager.createImportDialog(activity, new DialogManager.ImportDialogCallback() {
            @Override
            public void onInput(EditText et) {
                et.addTextChangedListener(new TextWatcherImpl() {
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        newGroupName = charSequence.toString();
                    }
                });
            }

            @Override
            public void onLeft(Button btn) {
                btn.setOnClickListener(new OnClickListenerImpl() {
                    @Override
                    public void onViewClick(View v, long interval) {
                        if (groupingDialog == null) {
                            initGroupDialog();
                        }
                        groupingDialog.show();
                    }
                });
            }

            @Override
            public void onCenter(Button btn) {
                btn.setOnClickListener(new OnClickListenerImpl() {
                    @Override
                    public void onViewClick(View v, long interval) {
                        setResultBack(SelectActivity.TYPE_BASE_FOLDER, null);
//                        DataManager.updatePDFs();
//                        DataManager.updateCollection();
//                        DBHelper.insert(selectList);
                    }
                });
            }

            @Override
            public void onRight(Button btn) {
                btn.setOnClickListener(new OnClickListenerImpl() {
                    @Override
                    public void onViewClick(View v, long interval) {
                        if (StringUtils.isEmpty(newGroupName)) {
                            UiManager.showShort(R.string.app_type_new_group_name);
                        } else {
                            setResultBack(SelectActivity.TYPE_CUSTOM, newGroupName);
//                            DataManager.updatePDFs();
//                            DataManager.updateCollection();
//                            DBHelper.insert(selectList, newGroupName);
                        }
                    }
                });
            }
        });
    }

    private void initGroupDialog() {
        groupingDialog = DialogManager.createGroupingDialog(activity, false, new GroupingAdapter.Callback() {
            @Override
            public void onAddNewGroup() {
//                if (inputDialog == null) {
//                    initInputDialog();
//                }
//                inputDialog.show();
                // empty
            }

            @Override
            public void onAddToGroup(String dir) {
                setResultBack(SelectActivity.TYPE_TO_EXIST, dir);
//                DataManager.updatePDFs();
//                DataManager.updateCollection();
//                DBHelper.insert(selectList, dir);
            }
        });
    }

    private void setResultBack(int type, String groupName) {
        Intent data = new Intent();
        data.putStringArrayListExtra(SelectActivity.EXTRA_SELECTED, (ArrayList<String>) selectList);
        data.putExtra(SelectActivity.EXTRA_TYPE, type);
        data.putExtra(SelectActivity.EXTRA_GROUP_NAME, groupName);
        activity.setResult(RESULT_OK, data);
        activity.finish();
    }
}
