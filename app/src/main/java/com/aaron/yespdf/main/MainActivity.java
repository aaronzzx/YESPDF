package com.aaron.yespdf.main;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.aaron.base.impl.TextWatcherImpl;
import com.aaron.yespdf.CommonActivity;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.filepicker.SelectActivity;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.google.android.material.tabs.TabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends CommonActivity {

//    @BindView(R2.id.app_et_search)  EditText mEtSearch;
//    @BindView(R2.id.app_ibtn_clear) ImageButton mIbtnClear;
    @BindView(R2.id.app_tab_layout) TabLayout mTabLayout;
    @BindView(R2.id.app_vp)         ViewPager mVp;

    private Unbinder mUnbinder;
    private PopupWindow mPwMenu;

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
        initView(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        KeyboardUtils.unregisterSoftInputChangedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.app_more) {
            View parent = getWindow().getDecorView();
            int x = ConvertUtils.dp2px(6);
            int y = ConvertUtils.dp2px(80);
            mPwMenu.showAtLocation(parent, Gravity.TOP | Gravity.END, x, y);
        }
        return true;
    }

    private void initView(Bundle savedInstanceState) {
        initPopupWindow();

        // 监听软键盘是否打开，如果是则让 EditText 显示光标，否则不显示。
//        KeyboardUtils.registerSoftInputChangedListener(this, height -> mEtSearch.setCursorVisible(height > 0));
//        mEtSearch.addTextChangedListener(new TextWatcherImpl() {
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (charSequence.length() != 0) {
//                    mIbtnClear.setVisibility(View.VISIBLE);
//                } else {
//                    mIbtnClear.setVisibility(View.GONE);
//                }
//            }
//        });
//        mIbtnClear.setOnClickListener(v -> {
//            mEtSearch.setText("");
//            if (!KeyboardUtils.isSoftInputVisible(MainActivity.this)) {
//                KeyboardUtils.showSoftInput(MainActivity.this);
//            }
//        });
        mTabLayout.setupWithViewPager(mVp);
        mVp.setAdapter(new MainFragmentAdapter(getSupportFragmentManager()));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void initPopupWindow() {
        View pwView = LayoutInflater.from(this).inflate(R.layout.app_pw_main, null);
        TextView tvImport   = pwView.findViewById(R.id.app_tv_import);
//        TextView tvSettings = pwView.findViewById(R.id.app_tv_settings);
        TextView tvAbout    = pwView.findViewById(R.id.app_tv_about);
        mPwMenu = new PopupWindow(pwView);
        tvImport.setOnClickListener(v -> {
            // TODO: 2019/9/4 导入 PDF 逻辑
            SelectActivity.start(this);
            mPwMenu.dismiss();
        });
//        tvSettings.setOnClickListener(v -> {
//            mPwMenu.dismiss();
//        });
        tvAbout.setOnClickListener(v -> {
            // TODO: 2019/9/4 App 关于逻辑
            mPwMenu.dismiss();
        });
        mPwMenu.setAnimationStyle(R.style.AppPopupWindow);
        mPwMenu.setFocusable(true);
        mPwMenu.setOutsideTouchable(true);
        mPwMenu.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMenu.setElevation(ConvertUtils.dp2px(2));
    }
}
