package com.aaron.yespdf.filepicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.aaron.base.impl.TextWatcherImpl;
import com.aaron.base.util.StatusBarUtils;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.CommonActivity;
import com.blankj.utilcode.util.KeyboardUtils;
import com.github.anzewei.parallaxbacklayout.ParallaxBack;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
@ParallaxBack
public class SelectActivity extends CommonActivity/* implements IViewAllContract.V*/ {

    public static final String EXTRA_SELECTED = "EXTRA_SELECTED";
    public static final String EXTRA_TYPE = "EXTRA_TYPE";
    public static final String EXTRA_GROUP_NAME = "EXTRA_GROUP_NAME";
    public static final int TYPE_TO_EXIST = 1;
    public static final int TYPE_BASE_FOLDER = 2;
    public static final int TYPE_CUSTOM = 3;

    public static final int REQUEST_CODE = 111;

    static final String EXTRA_IMPORTED = "EXTRA_IMPORTED";

    @BindView(R2.id.app_search_view)
    View searchView;
    @BindView(R2.id.app_ibtn_back)
    ImageButton ibtnCancelSearch;
    @BindView(R2.id.app_et_search)
    EditText etSearch;
    @BindView(R2.id.app_ibtn_clear)
    ImageButton ibtnClear;
    @BindView(R2.id.app_ibtn_check)
    ImageButton ibtnSelectAll;
    @BindView(R2.id.app_ibtn_search)
    ImageButton ibtnSearch;
    @BindView(R2.id.app_tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.app_vp)
    ViewPager vp;
//    @BindView(R2.id.app_horizontal_sv)
//    HorizontalScrollView horizontalSv;
//    @BindView(R2.id.app_ll)
//    ViewGroup vgPath;
//    @BindView(R2.id.app_tv_path)
//    TextView tvPath;
//    @BindView(R2.id.app_rv_select)
//    RecyclerView rvSelect;
//    @BindView(R2.id.app_tv_import_count)
//    TextView btnImportCount;
//
    List<String> importeds;

    // 揭露动画参数
    private int duration = 250;
    private int centerX;
    private int centerY;
    private float radius;
//
//    private IViewAllContract.P presenter;
    private Unbinder unbinder;
    private ViewAllAdapter viewAllAdapter;
    private FragmentPagerAdapter fragmentPagerAdapter;
//    private AbstractAdapter adapter;
//
//    private List<File> fileList = new ArrayList<>();
//    private List<String> selectList = new ArrayList<>();
//    private View.OnClickListener onClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            String path = (String) v.getTag();
//            int index = vgPath.indexOfChild(v);
//            int count = vgPath.getChildCount();
//            vgPath.removeViews(index + 1, count - index - 1);
//            presenter.listFile(path);
//        }
//    };
//    private RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
//        @Override
//        public void onChanged() {
//            ibtnSelectAll.setSelected(false);
//            btnImportCount.setText(R.string.app_import_count);
//            boolean enableSelectAll = adapter.reset();
//            ibtnSelectAll.setEnabled(enableSelectAll);
//        }
//    };

    public static void start(Activity activity, int requestCode, ArrayList<String> imported) {
        Intent starter = new Intent(activity, SelectActivity.class);
        starter.putStringArrayListExtra(EXTRA_IMPORTED, imported);
        activity.startActivityForResult(starter, requestCode);
    }

    void setViewAllAdapter(ViewAllAdapter viewAllAdapter) {
        this.viewAllAdapter = viewAllAdapter;
    }

    void setRevealParam() {
        searchView.post(() -> {
            centerX = ibtnSearch.getLeft() + ibtnSearch.getMeasuredWidth() / 2;
            centerY = ibtnSearch.getTop() + ibtnSearch.getMeasuredHeight() / 2;
            int width = centerX * 2;
            int height = searchView.getMeasuredHeight();
            radius = (float) (Math.sqrt(width * width + height * height) / 2);
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        initToolbar();
        toolbar.setPadding(0, 0, 0, 0);
        StatusBarUtils.setStatusBarLight(this, true);
//        attachP();
        initView();
//        presenter.listStorage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        adapter.unregisterAdapterDataObserver(dataObserver);
        unbinder.unbind();
//        presenter.detachV();
    }

//    @Override
//    public void onBackPressed() {
//        if (presenter.canFinish()) {
//            super.onBackPressed();
//        } else {
//            presenter.goBack();
//        }
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (searchView.getVisibility() == View.VISIBLE) {
            closeSearchView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected int layoutId() {
        return R.layout.app_activity_select;
    }

    @Override
    protected Toolbar createToolbar() {
        return findViewById(R.id.app_toolbar);
    }

//    @Override
//    public void attachP() {
//        presenter = new ViewAllPresenter(this);
//    }
//
//    @Override
//    public void onShowMessage(int stringId) {
//        UiManager.showShort(stringId);
//    }
//
//    @Override
//    public void onShowFileList(List<File> fileList) {
//        this.fileList.clear();
//        this.fileList.addAll(fileList);
//        adapter.notifyDataSetChanged();
//    }
//
//    @Override
//    public void onShowPath(List<String> pathList) {
//        vgPath.removeViews(1, vgPath.getChildCount() - 1);
//        LayoutInflater inflater = LayoutInflater.from(this);
//        StringBuilder parent = new StringBuilder(IViewAllContract.P.ROOT_PATH);
//        for (String dirName : pathList) {
//            TextView tvPath = (TextView) inflater.inflate(R.layout.app_include_tv_path, null);
//            tvPath.setOnClickListener(onClickListener);
//            tvPath.setText(dirName);
//            parent.append("/").append(dirName); // 当前节点生成后即变成下一节点的父节点
//            tvPath.setTag(parent.toString());
//            vgPath.addView(tvPath);
//        }
//    }
//
//    void onDirTap(String dirPath) {
//        presenter.listFile(dirPath);
//    }
//
//    @SuppressLint("SetTextI18n")
//    void onSelectResult(List<String> pathList, int total) {
//        LogUtils.e(pathList);
//        ibtnSelectAll.setSelected(pathList.size() == total);
//        btnImportCount.setText(getString(R.string.app_import) + "(" + pathList.size() + ")");
//        selectList.clear();
//        selectList.addAll(pathList);
//    }


    private void initView() {
        Intent data = getIntent();
        importeds = data.getStringArrayListExtra(EXTRA_IMPORTED);

//        searchView.post(() -> {
//            centerX = ibtnSearch.getLeft() + ibtnSearch.getMeasuredWidth() / 2;
//            centerY = ibtnSearch.getTop() + ibtnSearch.getMeasuredHeight() / 2;
//            int width = centerX * 2;
//            int height = searchView.getMeasuredHeight();
//            radius = (float) (Math.sqrt(width * width + height * height) / 2);
//            LogUtils.e("centerX: " + centerX);
//            LogUtils.e("centerY: " + centerY);
//            LogUtils.e("radius: " + radius);
//        });

        ibtnCancelSearch.setOnClickListener(v -> {
            closeSearchView();
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
                viewAllAdapter.getFilter().filter(c);
            }
        });

        tabLayout.setupWithViewPager(vp);
        fragmentPagerAdapter = new SelectFragmentAdapter(getSupportFragmentManager());
        vp.setAdapter(fragmentPagerAdapter);

//        tvPath.setOnClickListener(onClickListener);
//        tvPath.setTag(IViewAllContract.P.ROOT_PATH); // 原始路径
//        btnImportCount.setOnClickListener(new OnClickListenerImpl() {
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onViewClick(View v, long interval) {
//                if (selectList.isEmpty()) {
//                    UiManager.showShort(R.string.app_have_not_select);
//                } else {
//                    Intent data = new Intent();
//                    data.putStringArrayListExtra(EXTRA_SELECTED, (ArrayList<String>) selectList);
//                    setResult(RESULT_OK, data);
//                    finish();
//                }
//            }
//        });
//        ibtnSelectAll.setOnClickListener(v -> {
//            ibtnSelectAll.setSelected(!ibtnSelectAll.isSelected());
//            adapter.selectAll(ibtnSelectAll.isSelected());
//        });
//
//        ibtnSelectAll.setEnabled(false); // XML 设置无效，只能这里初始化
//        RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
//        rvSelect.setLayoutManager(lm);
//        adapter = new SelectAdapter(fileList, importeds);
//        adapter.registerAdapterDataObserver(dataObserver);
//        rvSelect.setAdapter(adapter);
    }

    private void initToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.app_ic_action_back_black);
        }
        toolbar.setTitle(R.string.app_import_file);
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
                KeyboardUtils.showSoftInput(SelectActivity.this);
            }
        });
        animator.start();
    }

    void closeSearchView() {
        Animator animator = ViewAnimationUtils.createCircularReveal(searchView, centerX, centerY, radius, 0);
        animator.setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (KeyboardUtils.isSoftInputVisible(SelectActivity.this)) {
                    KeyboardUtils.hideSoftInput(SelectActivity.this);
                }
                searchView.setVisibility(View.GONE);
            }
        });
        animator.start();
    }
}
