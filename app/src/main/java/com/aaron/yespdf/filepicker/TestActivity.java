package com.aaron.yespdf.filepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class TestActivity extends CommonActivity implements ISelectContract.V, ITestActivityInterface {

    public static final String EXTRA_SELECTED = "EXTRA_SELECTED";
    private static final String EXTRA_IMPORTED = "EXTRA_IMPORTED";

    @BindView(R2.id.app_iv_check)
    ImageView mIvSelectAll;
    @BindView(R2.id.app_horizontal_sv)
    HorizontalScrollView mHorizontalSv;
    @BindView(R2.id.app_ll)
    ViewGroup mVgPath;
    @BindView(R2.id.app_tv_path)
    TextView mTvPath;
    @BindView(R2.id.app_rv_select)
    RecyclerView mRvSelect;
    @BindView(R2.id.app_tv_import_count)
    TextView mTvImportCount;

    private ISelectContract.P mP;
    private Unbinder mUnbinder;
    private RecyclerView.Adapter mAdapter;

    private List<File> mFileList = new ArrayList<>();
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String path = (String) v.getTag();
            int index = mVgPath.indexOfChild(v);
            int count = mVgPath.getChildCount();
            mVgPath.removeViews(index + 1, count - index - 1);
            mP.listFile(path);
        }
    };
    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            mIvSelectAll.setSelected(false);
            mTvImportCount.setText(R.string.app_import_count);
            ((ITestAdapterInterface) mAdapter).reset();
        }
    };

    public static void start(Activity activity, int requestCode, ArrayList<String> imported) {
        Intent starter = new Intent(activity, TestActivity.class);
        starter.putStringArrayListExtra(EXTRA_IMPORTED, imported);
        activity.startActivityForResult(starter, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attachP();
        mUnbinder = ButterKnife.bind(this);
        initToolbar();
        initView();
        mP.listStorage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.unregisterAdapterDataObserver(mDataObserver);
        mUnbinder.unbind();
        mP.detachV();
    }

    @Override
    public void onBackPressed() {
        if (mP.canFinish()) {
            super.onBackPressed();
        } else {
            mP.goBack();
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
        mP = new SelectP(this);
    }

    @Override
    public void onShowMessage(int stringId) {
        UiManager.showShort(stringId);
    }

    @Override
    public void onShowFileList(List<File> fileList) {
        mFileList.clear();
        mFileList.addAll(fileList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onShowPath(List<String> pathList) {
        mVgPath.removeViews(1, mVgPath.getChildCount() - 1);
        LayoutInflater inflater = LayoutInflater.from(this);
        StringBuilder parent = new StringBuilder(ISelectContract.P.ROOT_PATH);
        for (String dirName : pathList) {
            TextView tvPath = (TextView) inflater.inflate(R.layout.app_include_tv_path, null);
            tvPath.setOnClickListener(mOnClickListener);
            tvPath.setText(dirName);
            parent.append("/").append(dirName); // 当前节点生成后即变成下一节点的父节点
            tvPath.setTag(parent.toString());
            mVgPath.addView(tvPath);
        }
    }

    @Override
    public void onDirTap(String dirPath) {
        mP.listFile(dirPath);
    }

    @Override
    public View getViewSelectAll() {
        return mIvSelectAll;
    }

    private void initView() {
        Intent data = getIntent();
        List<String> importeds = data.getStringArrayListExtra(EXTRA_IMPORTED);

        mTvPath.setOnClickListener(mOnClickListener);
        mTvPath.setTag(ISelectContract.P.ROOT_PATH); // 原始路径
        mTvImportCount.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                List<String> selectResult = ((ITestAdapterInterface) mAdapter).selectResult();
                if (selectResult.isEmpty()) {
                    UiManager.showShort(R.string.app_have_not_select);
                } else {
                    Intent data = new Intent();
                    data.putStringArrayListExtra(EXTRA_SELECTED, (ArrayList<String>) selectResult);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });
        mIvSelectAll.setOnClickListener(v -> {
            mIvSelectAll.setSelected(!mIvSelectAll.isSelected());
            ((ITestAdapterInterface) mAdapter).selectAll(mIvSelectAll.isSelected());
        });

        RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
        mRvSelect.setLayoutManager(lm);
        mAdapter = new TestAdapter(mFileList, importeds);
        mAdapter.registerAdapterDataObserver(mDataObserver);
        mRvSelect.setAdapter(mAdapter);
    }

    private void initToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.app_ic_action_back_black);
        }
        mToolbar.setTitle(R.string.app_import_file);
    }
}
