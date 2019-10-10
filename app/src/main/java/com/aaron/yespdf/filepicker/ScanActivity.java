package com.aaron.yespdf.filepicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.impl.OnClickListenerImpl;
import com.aaron.base.impl.TextWatcherImpl;
import com.aaron.base.util.StatusBarUtils;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.CommonActivity;
import com.aaron.yespdf.common.DialogManager;
import com.aaron.yespdf.common.GroupingAdapter;
import com.aaron.yespdf.common.UiManager;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SDCardUtils;
import com.blankj.utilcode.util.StringUtils;
import com.github.anzewei.parallaxbacklayout.ParallaxBack;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@ParallaxBack
public class ScanActivity extends CommonActivity {

    static final String EXTRA_IMPORTED = "EXTRA_IMPORTED";

    @BindView(R2.id.app_search_view)
    View searchView;
    @BindView(R2.id.app_ibtn_back)
    ImageButton ibtnCancelSearch;
    @BindView(R2.id.app_et_search)
    EditText etSearch;
    @BindView(R2.id.app_ibtn_clear)
    ImageButton ibtnClear;
    @BindView(R2.id.app_ibtn_search)
    ImageButton ibtnSearch;
    @BindView(R2.id.app_ibtn_select_all)
    ImageButton ibtnSelectAll;
    @BindView(R2.id.app_rv_select)
    RecyclerView rv;
    @BindView(R2.id.app_tv_import_count)
    TextView tvImport;

    // 揭露动画参数
    private int duration = 250;
    private int centerX;
    private int centerY;
    private float radius;

    private List<File> fileList = new ArrayList<>();
    private List<String> selectList = new ArrayList<>();

    private TextView tvScanCount;
    private TextView tvPdfCount;
    private Disposable scanDisp;
    private int scanCount;
    private int pdfCount;
    private boolean stopScan;
    private String newGroupName;

    private ExecutorService threadPool;
    private int traverseFileCount = 0;

    private Unbinder unbinder;
    private AbstractAdapter adapter;
    private BottomSheetDialog scanDialog;
    private BottomSheetDialog importDialog;
    private BottomSheetDialog groupingDialog;
    private IListable listable = new ByNameListable();
    private RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (!isFinishing()) {
                ibtnSelectAll.setSelected(false);
                tvImport.setText(R.string.app_import_count);
                boolean enableSelectAll = adapter.reset();
                ibtnSelectAll.setEnabled(enableSelectAll);
            }
        }
    };

    public static void start(Activity activity, ArrayList<String> imported, int requestCode) {
        Intent starter = new Intent(activity, ScanActivity.class);
        starter.putStringArrayListExtra(EXTRA_IMPORTED, imported);
        activity.startActivityForResult(starter, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        initToolbar();
        toolbar.setPadding(0, 0, 0, 0);
        StatusBarUtils.setStatusBarLight(this, true);
        initView();
    }

    @Override
    protected int layoutId() {
        return R.layout.app_activity_scan;
    }

    @Override
    protected Toolbar createToolbar() {
        return findViewById(R.id.app_toolbar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        List<String> imported = getIntent().getStringArrayListExtra(EXTRA_IMPORTED);

        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        scanDialog = DialogManager.createScanDialog(this, new DialogManager.ScanDialogCallback() {
            @Override
            public void onTitle(TextView tv) {
                tvScanCount = tv;
                tvScanCount.setText(getString(R.string.app_already_scan) + 0 + getString(R.string.app_file_));
            }

            @Override
            public void onContent(TextView tv) {
                tvPdfCount = tv;
                tvPdfCount.setText(getString(R.string.app_find) + "PDF(" + 0 + ")");
            }

            @Override
            public void onButton(Button btn) {
                btn.setOnClickListener(new OnClickListenerImpl() {
                    @Override
                    public void onViewClick(View v, long interval) {
                        scanDialog.dismiss();
                        stopScan = true;
                        threadPool.shutdownNow();
//                        if (scanDisp != null && !scanDisp.isDisposed()) {
//                            scanDisp.dispose();
//
//                        }
                    }
                });
            }
        });

        searchView.post(() -> {
            centerX = ibtnSearch.getLeft() + ibtnSearch.getMeasuredWidth() / 2;
            centerY = ibtnSearch.getTop() + ibtnSearch.getMeasuredHeight() / 2;
            int width = centerX * 2;
            int height = searchView.getMeasuredHeight();
            radius = (float) (Math.sqrt(width * width + height * height) / 2);
        });

        ibtnCancelSearch.setOnClickListener(v -> {
            closeSearchView();
            etSearch.setText("");
        });
        ibtnSearch.setOnClickListener(v -> openSearchView());
        ibtnClear.setOnClickListener(v -> {
            etSearch.setText("");
            if (!KeyboardUtils.isSoftInputVisible(this)) {
                KeyboardUtils.showSoftInput(this);
            }
        });
        etSearch.addTextChangedListener(new TextWatcherImpl() {
            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {
                ibtnClear.setVisibility(c.length() == 0 ? View.GONE : View.VISIBLE);
            }
        });
        ibtnSelectAll.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            adapter.selectAll(v.isSelected());
        });
        tvImport.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
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

        ibtnSelectAll.setEnabled(false); // XML 设置无效，只能这里初始化
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        adapter = new ViewAllAdapter(fileList, imported, new ViewAllAdapter.Callback() {
            @Override
            public void onDirTap(String dirPath) {
                // empty impl
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onSelectResult(List<String> pathList, int total) {
                ibtnSelectAll.setSelected(pathList.size() == total);
                tvImport.setText(getString(R.string.app_import) + "(" + pathList.size() + ")");
                selectList.clear();
                selectList.addAll(pathList);
            }
        });
        adapter.registerAdapterDataObserver(dataObserver);
        rv.setAdapter(adapter);

        scanDialog.show();
        scanDisp = Observable.create((ObservableOnSubscribe<Double>) emitter -> {
                    emitter.onNext(traverseFile());
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(cost -> {
                            scanDialog.dismiss();
                            adapter.notifyDataSetChanged();
                    if (!stopScan) {
                        UiManager.showShort(getString(R.string.app_scan_finish) + cost + getString(R.string.app_second_has_blank));
                    }
                }, throwable -> LogUtils.e(throwable.getMessage()));
    }

    private double traverseFile() {
        long start = System.currentTimeMillis();
        List<SDCardUtils.SDCardInfo> result = SDCardUtils.getSDCardInfo();
        for (SDCardUtils.SDCardInfo info : result) {
            if (!stopScan && "mounted".equals(info.getState())) {
                traverse(new File(info.getPath()));
            }
        }
        while (true) {
            int temp = traverseFileCount;
            SystemClock.sleep(500);
            if (temp == traverseFileCount) {
                long end = System.currentTimeMillis();
                double cost = (double) (end - start - 500) / 1000;
                LogUtils.e("总共耗时：" + cost + " 秒");
                return cost;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private synchronized void traverse(File file) {
        if (stopScan) {
            return;
        }
        threadPool.execute(() -> {
            List<File> fileList = listable.listFile(file.getAbsolutePath());
            traverseFileCount += fileList.size();
            for (File f : fileList) {
                if (stopScan) {
                    return;
                }
                runOnUiThread(() -> {
                    scanCount++;
                    tvScanCount.setText(getString(R.string.app_already_scan) + scanCount + getString(R.string.app_file_));
                });
                if (f.isFile()) {
                    this.fileList.add(f);
                    runOnUiThread(() -> {
                        pdfCount++;
                        tvPdfCount.setText(getString(R.string.app_find) + "PDF(" + pdfCount + ")");
                        adapter.notifyDataSetChanged();
                    });
                } else {
                    traverse(f);
                }
            }
        });
    }

    private void initImportDialog() {
        importDialog = DialogManager.createImportDialog(this, new DialogManager.ImportDialogCallback() {
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
        groupingDialog = DialogManager.createGroupingDialog(ScanActivity.this, false, new GroupingAdapter.Callback() {
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

//    private void initInputDialog() {
//        inputDialog = DialogManager.createInputDialog(ScanActivity.this, new DialogManager.InputDialogCallback() {
//            @Override
//            public void onTitle(TextView tv) {
//                tv.setText(R.string.app_add_new_group);
//            }
//
//            @Override
//            public void onInput(EditText et) {
//                etInput = et;
//                et.setInputType(InputType.TYPE_CLASS_TEXT);
//                et.setHint(R.string.app_type_new_group_name);
//            }
//
//            @Override
//            public void onLeft(Button btn) {
//                btn.setText(R.string.app_cancel);
//                btn.setOnClickListener(v -> inputDialog.dismiss());
//            }
//
//            @Override
//            public void onRight(Button btn) {
//                btn.setText(R.string.app_confirm);
//                btn.setOnClickListener(v -> {
//                    DataManager.updatePDFs();
//                    DataManager.updateCollection();
//                    DBHelper.insert(selectList, etInput.getText().toString());
//                });
//            }
//        });
//    }

    private void initToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.app_ic_action_back_black);
        }
        toolbar.setTitle(R.string.app_auto_scan);
    }

    private void openSearchView() {
        Animator animator = ViewAnimationUtils.createCircularReveal(searchView, centerX, centerY, 0, radius);
        animator.setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                searchView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                etSearch.requestFocus();
                KeyboardUtils.showSoftInput(ScanActivity.this);
            }
        });
        animator.start();
    }

    private void closeSearchView() {
        Animator animator = ViewAnimationUtils.createCircularReveal(searchView, centerX, centerY, radius, 0);
        animator.setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (KeyboardUtils.isSoftInputVisible(ScanActivity.this)) {
                    KeyboardUtils.hideSoftInput(ScanActivity.this);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                searchView.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    private void setResultBack(int type, String groupName) {
        Intent data = new Intent();
        data.putStringArrayListExtra(SelectActivity.EXTRA_SELECTED, (ArrayList<String>) selectList);
        data.putExtra(SelectActivity.EXTRA_TYPE, type);
        data.putExtra(SelectActivity.EXTRA_GROUP_NAME, groupName);
        setResult(RESULT_OK, data);
        finish();
    }
}
