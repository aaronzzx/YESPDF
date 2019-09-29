package com.aaron.yespdf.filepicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.impl.OnClickListenerImpl;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.CommonActivity;
import com.aaron.yespdf.common.UiManager;
import com.blankj.utilcode.util.LogUtils;
import com.github.anzewei.parallaxbacklayout.ParallaxBack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
@ParallaxBack
public class SelectActivity extends CommonActivity implements ISelectContract.V {

    public static final String EXTRA_SELECTED = "EXTRA_SELECTED";
    private static final String EXTRA_IMPORTED = "EXTRA_IMPORTED";

    @BindView(R2.id.app_ibtn_check)
    ImageButton ibtnSelectAll;
    @BindView(R2.id.app_horizontal_sv)
    HorizontalScrollView horizontalSv;
    @BindView(R2.id.app_ll)
    ViewGroup vgPath;
    @BindView(R2.id.app_tv_path)
    TextView tvPath;
    @BindView(R2.id.app_rv_select)
    RecyclerView rvSelect;
    @BindView(R2.id.app_tv_import_count)
    TextView tvImportCount;

    private ISelectContract.P presenter;
    private Unbinder unbinder;
    private AbstractAdapter adapter;

    private List<File> fileList = new ArrayList<>();
    private List<String> selectList = new ArrayList<>();
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
            ibtnSelectAll.setSelected(false);
            tvImportCount.setText(R.string.app_import_count);
            boolean enableSelectAll = adapter.reset();
            ibtnSelectAll.setEnabled(enableSelectAll);
        }
    };

    public static void start(Activity activity, int requestCode, ArrayList<String> imported) {
        Intent starter = new Intent(activity, SelectActivity.class);
        starter.putStringArrayListExtra(EXTRA_IMPORTED, imported);
        activity.startActivityForResult(starter, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attachP();
        unbinder = ButterKnife.bind(this);
        initToolbar();
        initView();
        presenter.listStorage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.unregisterAdapterDataObserver(dataObserver);
        unbinder.unbind();
        presenter.detachV();
    }

    @Override
    public void onBackPressed() {
        if (presenter.canFinish()) {
            super.onBackPressed();
        } else {
            presenter.goBack();
        }
    }

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

    @Override
    public void attachP() {
        presenter = new SelectP(this);
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
        LayoutInflater inflater = LayoutInflater.from(this);
        StringBuilder parent = new StringBuilder(ISelectContract.P.ROOT_PATH);
        for (String dirName : pathList) {
            TextView tvPath = (TextView) inflater.inflate(R.layout.app_include_tv_path, null);
            tvPath.setOnClickListener(onClickListener);
            tvPath.setText(dirName);
            parent.append("/").append(dirName); // 当前节点生成后即变成下一节点的父节点
            tvPath.setTag(parent.toString());
            vgPath.addView(tvPath);
        }
    }

    void onDirTap(String dirPath) {
        presenter.listFile(dirPath);
    }

    @SuppressLint("SetTextI18n")
    void onSelectResult(List<String> pathList, int total) {
        LogUtils.e(pathList);
        ibtnSelectAll.setSelected(pathList.size() == total);
        tvImportCount.setText(getString(R.string.app_import) + "(" + pathList.size() + ")");
        selectList.clear();
        selectList.addAll(pathList);
    }

    private void initView() {
        Intent data = getIntent();
        List<String> importeds = data.getStringArrayListExtra(EXTRA_IMPORTED);

        tvPath.setOnClickListener(onClickListener);
        tvPath.setTag(ISelectContract.P.ROOT_PATH); // 原始路径
        tvImportCount.setOnClickListener(new OnClickListenerImpl() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onViewClick(View v, long interval) {
                if (selectList.isEmpty()) {
                    UiManager.showShort(R.string.app_have_not_select);
                } else {
                    Intent data = new Intent();
                    data.putStringArrayListExtra(EXTRA_SELECTED, (ArrayList<String>) selectList);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });
        ibtnSelectAll.setOnClickListener(v -> {
            ibtnSelectAll.setSelected(!ibtnSelectAll.isSelected());
            adapter.selectAll(ibtnSelectAll.isSelected());
        });

        ibtnSelectAll.setEnabled(false); // XML 设置无效，只能这里初始化
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
        rvSelect.setLayoutManager(lm);
        adapter = new SelectAdapter(fileList, importeds);
        adapter.registerAdapterDataObserver(dataObserver);
        rvSelect.setAdapter(adapter);
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
