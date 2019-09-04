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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SelectActivity extends CommonActivity implements View.OnClickListener {

    @BindView(R2.id.app_ll) ViewGroup mVgPath;
    @BindView(R2.id.app_tv_path) TextView mTvPath;
    @BindView(R2.id.app_rv_select) RecyclerView mRvSelect;

    private Unbinder mUnbinder;

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
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onClick(View view) {
        LayoutInflater inflater = LayoutInflater.from(this);
        TextView tvPath = (TextView) inflater.inflate(R.layout.app_include_tv_path, null);
        tvPath.setOnClickListener(this);
        tvPath.setText("Android#Java");
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

        RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
        mRvSelect.setLayoutManager(lm);
        RecyclerView.Adapter adapter = new SelectAdapter();
        mRvSelect.setAdapter(adapter);
    }
}
