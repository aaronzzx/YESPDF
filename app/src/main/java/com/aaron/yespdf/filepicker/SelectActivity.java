package com.aaron.yespdf.filepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.aaron.base.util.StatusBarUtils;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.CommonActivity;
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
    static final String EXTRA_IMPORTED = "EXTRA_IMPORTED";

    @BindView(R2.id.app_ibtn_check)
    ImageButton ibtnSelectAll;
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
//    TextView tvImportCount;
//
    List<String> importeds;
//
//    private IViewAllContract.P presenter;
    private Unbinder unbinder;
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
//            tvImportCount.setText(R.string.app_import_count);
//            boolean enableSelectAll = adapter.reset();
//            ibtnSelectAll.setEnabled(enableSelectAll);
//        }
//    };

    public static void start(Activity activity, int requestCode, ArrayList<String> imported) {
        Intent starter = new Intent(activity, SelectActivity.class);
        starter.putStringArrayListExtra(EXTRA_IMPORTED, imported);
        activity.startActivityForResult(starter, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar.setPadding(0, 0, 0, 0);
        StatusBarUtils.setStatusBarLight(this, true);
//        attachP();
        unbinder = ButterKnife.bind(this);
        initToolbar();
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
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
//        tvImportCount.setText(getString(R.string.app_import) + "(" + pathList.size() + ")");
//        selectList.clear();
//        selectList.addAll(pathList);
//    }

    private void initView() {
        Intent data = getIntent();
        importeds = data.getStringArrayListExtra(EXTRA_IMPORTED);

        tabLayout.setupWithViewPager(vp);
        fragmentPagerAdapter = new SelectFragmentAdapter(getSupportFragmentManager());
        vp.setAdapter(fragmentPagerAdapter);

//        tvPath.setOnClickListener(onClickListener);
//        tvPath.setTag(IViewAllContract.P.ROOT_PATH); // 原始路径
//        tvImportCount.setOnClickListener(new OnClickListenerImpl() {
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
}
