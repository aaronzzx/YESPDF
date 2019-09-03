package com.aaron.yespdf;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.aaron.base.impl.TextWatcherImpl;
import com.blankj.utilcode.util.StringUtils;
import com.google.android.material.tabs.TabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends CommonActivity {

    @BindView(R2.id.app_et_search)  EditText mEtSearch;
    @BindView(R2.id.app_ibtn_clear) ImageButton mIbtnClear;
    @BindView(R2.id.app_tab_layout) TabLayout mTabLayout;
    @BindView(R2.id.app_vp)         ViewPager mVp;

    private Unbinder mUnbinder;

    @Override
    protected int layoutId() {
        return R.layout.app_activity_main;
    }

    @Override
    protected Toolbar createToolbar() {
        return findViewById(R.id.app_toolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUnbinder = ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_main, menu);
        return true;
    }

    private void initView() {
        mEtSearch.setOnClickListener(v -> mEtSearch.setCursorVisible(true));
        mEtSearch.setOnEditorActionListener((textView, i, keyEvent) -> {
            mEtSearch.setCursorVisible(false);
            return false;
        });
        mEtSearch.addTextChangedListener(new TextWatcherImpl() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) {
                    mIbtnClear.setVisibility(View.VISIBLE);
                } else {
                    mIbtnClear.setVisibility(View.GONE);
                }
            }
        });
        mIbtnClear.setOnClickListener(v -> mEtSearch.setText(""));
        mTabLayout.setupWithViewPager(mVp);
    }
}
