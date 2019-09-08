package com.aaron.yespdf.main;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.about.AboutActivity;
import com.aaron.yespdf.common.CommonActivity;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.UiManager;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.utils.DialogUtils;
import com.aaron.yespdf.filepicker.SelectActivity;
import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.github.anzewei.parallaxbacklayout.ParallaxBack;
import com.github.mmin18.widget.RealtimeBlurView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

@ParallaxBack
public class MainActivity extends CommonActivity implements AllAdapterComm {

    static final int SELECT_REQUEST_CODE = 101;

    //    @BindView(R2.id.app_et_search)  EditText mEtSearch;
//    @BindView(R2.id.app_ibtn_clear) ImageButton mIbtnClear;
    @BindView(R2.id.app_tab_layout) TabLayout mTabLayout;
    @BindView(R2.id.app_vp) ViewPager mVp;
    @BindView(R2.id.app_blur_view) RealtimeBlurView mBlurView;

    private Unbinder mUnbinder;
    private Dialog mLoadingDialog;
    private PopupWindow mPwMenu;
    private PopupWindow mPwCollection;

    private TextView mTvCollectionTitle;
    private RecyclerView mRvCollection;
    private RecyclerView.Adapter mCollectionAdapter;
    private FragmentPagerAdapter mFragmentPagerAdapter;

    private List<PDF> mPDFList = new ArrayList<>();

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
        PermissionUtils.permission(PermissionConstants.STORAGE)
                .request();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                mLoadingDialog.show();
                List<String> pathList = data.getStringArrayListExtra(SelectActivity.EXTRA_SELECTED);
                insertPDF(pathList);
            }
        }
    }

    private void insertPDF(List<String> pathList) {
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
            @NonNull
            @Override
            public Boolean doInBackground() {
                return DBHelper.insert(pathList);
            }

            @Override
            public void onSuccess(Boolean success) {
                mLoadingDialog.dismiss();
                if (success) {
                    UiManager.showShort(R.string.app_import_success);
                    List<Fragment> list = getSupportFragmentManager().getFragments();
                    for (Fragment f : list) {
                        if (f instanceof AllFragmentComm) {
                            ((AllFragmentComm) f).update(pathList);
                            break;
                        }
                    }
                } else {
                    UiManager.showShort(R.string.app_import_failure);
                }
            }
        });
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

    @Override
    public void onTap(String name) {
        mTvCollectionTitle.setText(name);
        mPDFList.clear();
        mPDFList.addAll(DBHelper.queryPDF(name));
        mCollectionAdapter.notifyDataSetChanged();
        mBlurView.animate().alpha(1).setDuration(200).start();
        mPwCollection.showAtLocation(mVp, Gravity.CENTER, 0, 0);
    }

    private void initView(Bundle savedInstanceState) {
        mLoadingDialog = DialogUtils.createDialog(this, R.layout.app_dialog_loading);

        initPwMenu();
        initPwCollection();

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
        mFragmentPagerAdapter = new MainFragmentAdapter(getSupportFragmentManager());
        mVp.setAdapter(mFragmentPagerAdapter);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void initPwMenu() {
        View pwView = LayoutInflater.from(this).inflate(R.layout.app_pw_main, null);
        TextView tvImport   = pwView.findViewById(R.id.app_tv_import);
        TextView tvSettings = pwView.findViewById(R.id.app_tv_settings);
        TextView tvAbout    = pwView.findViewById(R.id.app_tv_about);
        mPwMenu = new PopupWindow(pwView);
        tvImport.setOnClickListener(v -> {
            SelectActivity.start(this, SELECT_REQUEST_CODE);
            mPwMenu.dismiss();
        });
        tvSettings.setOnClickListener(v -> {
            mPwMenu.dismiss();
        });
        tvAbout.setOnClickListener(v -> {
            AboutActivity.start(this);
            mPwMenu.dismiss();
        });
        mPwMenu.setAnimationStyle(R.style.AppPwMenu);
        mPwMenu.setFocusable(true);
        mPwMenu.setOutsideTouchable(true);
        mPwMenu.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMenu.setElevation(ConvertUtils.dp2px(4));
    }

    private void initPwCollection() {
        View pwView = LayoutInflater.from(this).inflate(R.layout.app_pw_collection, null);
        mTvCollectionTitle = pwView.findViewById(R.id.app_tv_title);
        mRvCollection = pwView.findViewById(R.id.app_rv_collection);
        mRvCollection.addItemDecoration(new XGridDecoration());
        mRvCollection.addItemDecoration(new YGridDecoration());
        RecyclerView.LayoutManager lm = new GridLayoutManager(this, 3);
        mRvCollection.setLayoutManager(lm);

        mCollectionAdapter = new PDFAdapter(mPDFList);
        mRvCollection.setAdapter(mCollectionAdapter);
        mPwCollection = new PopupWindow(pwView);
        mPwCollection.setOnDismissListener(() -> {
            mRvCollection.scrollToPosition(0);
            mBlurView.animate().alpha(0).setDuration(200).start();
        });

        mPwCollection.setAnimationStyle(R.style.AppPwCollection);
        mPwCollection.setFocusable(true);
        mPwCollection.setOutsideTouchable(true);
        mPwCollection.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPwCollection.setHeight(ConvertUtils.dp2px(480));
        mPwCollection.setElevation(ConvertUtils.dp2px(2));
    }
}
