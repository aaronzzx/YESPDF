package com.aaron.yespdf.filepicker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.CommonActivity;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SelectActivity extends CommonActivity implements View.OnClickListener, Communicable {

    @BindView(R2.id.app_ll) ViewGroup mVgPath;
    @BindView(R2.id.app_tv_path) TextView mTvPath;
    @BindView(R2.id.app_rv_select) RecyclerView mRvSelect;

    private Unbinder mUnbinder;
    private Listable mListable;
    private RecyclerView.Adapter mAdapter;
    private List<File> mFileList = new ArrayList<>();

    private String mPreviousPath = "";
    private String mCurrentPath = "/storage/emulated/0";

    public static void start(Context context) {
        Intent starter = new Intent(context, SelectActivity.class);
        context.startActivity(starter);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUnbinder = ButterKnife.bind(this);
        initToolbar();
        initView(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        if (mCurrentPath.endsWith("0")) {
            super.onBackPressed();
        } else {
            mPreviousPath = mCurrentPath.substring(0, mCurrentPath.lastIndexOf("/"));
            handleJump(mPreviousPath);
            backPath();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onClick(View view) {
        String path = (String) view.getTag();
        handleJump(path);
        jumpPath(view);
    }

    @Override
    public void onTap(View view, String path) {
        LogUtils.e(path);
        handleJump(path);
        setCurPath(path);
    }

    private void jumpPath(View view) {
        int index = mVgPath.indexOfChild(view);
        int count = mVgPath.getChildCount();
        mVgPath.removeViews(index + 1,count - index - 1);
    }

    private void backPath() {
        int index = mVgPath.getChildCount() - 1;
        mVgPath.removeViews(index,1);
    }

    private void handleJump(String path) {
        mPreviousPath = mCurrentPath;
        mCurrentPath = path;
        mFileList.clear();
        mFileList.addAll(mListable.listFile(path));
        mAdapter.notifyDataSetChanged();
    }

    private void setCurPath(String path) {
        String curPath = path.substring(path.lastIndexOf("/") + 1);
        LayoutInflater inflater = LayoutInflater.from(this);
        TextView tvPath = (TextView) inflater.inflate(R.layout.app_include_tv_path, null);
        tvPath.setOnClickListener(this);
        tvPath.setText(curPath);
        tvPath.setTag(path);
        mVgPath.addView(tvPath);
    }

    private void initToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.app_ic_action_back_black);
        }
        mToolbar.setTitle(R.string.app_import_pdf);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.base_black));
        mToolbar.setContentInsetStartWithNavigation(0);
    }

    private void initView(Bundle savedInstanceState) {
        mTvPath.setOnClickListener(this);
        mTvPath.setTag(mCurrentPath);

        RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
        mRvSelect.setLayoutManager(lm);
        mListable = new ByNameListable();
        mFileList = mListable.listFile(PathUtils.getExternalStoragePath());
        mAdapter = new SelectAdapter(mFileList);
        mRvSelect.setAdapter(mAdapter);
    }
}
