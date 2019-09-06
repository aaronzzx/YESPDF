package com.aaron.yespdf.main;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.BlurUtils;
import com.aaron.yespdf.common.CommonActivity;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.UiManager;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.utils.DialogUtils;
import com.aaron.yespdf.filepicker.SelectActivity;
import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.github.anzewei.parallaxbacklayout.ParallaxBack;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

@ParallaxBack
public class MainActivity extends CommonActivity implements Communicable {

    static final int SELECT_REQUEST_CODE = 101;

    //    @BindView(R2.id.app_et_search)  EditText mEtSearch;
//    @BindView(R2.id.app_ibtn_clear) ImageButton mIbtnClear;
    @BindView(R2.id.app_tab_layout) TabLayout mTabLayout;
    @BindView(R2.id.app_vp) ViewPager mVp;
    @BindView(R2.id.app_black_cover) View mBlackCover;

    private Unbinder mUnbinder;
    private Dialog mLoadingDialog;
    private PopupWindow mPwMenu;
    private PopupWindow mPwCollection;

    private TextView mTvCollectionTitle;
    private RecyclerView mRvCollection;
    private RecyclerView.Adapter mCollectionAdapter;

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
                if (pathList != null) {
                    ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Object>() {
                        @Nullable
                        @Override
                        public Object doInBackground() throws Throwable {
                            DBHelper.insert(pathList);
                            return null;
                        }

                        @Override
                        public void onSuccess(@Nullable Object result) {
                            mLoadingDialog.dismiss();
                            UiManager.showShort(R.string.app_import_success);
                        }
                    });
                }
            }
        }
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
        mPwCollection.showAtLocation(mVp, Gravity.CENTER, 0, 0);
        showBlackCover();
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
        mVp.setAdapter(new MainFragmentAdapter(getSupportFragmentManager()));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void initPwMenu() {
        View pwView = LayoutInflater.from(this).inflate(R.layout.app_pw_main, null);
        TextView tvImport = pwView.findViewById(R.id.app_tv_import);
//        TextView tvSettings = pwView.findViewById(R.id.app_tv_settings);
        TextView tvAbout = pwView.findViewById(R.id.app_tv_about);
        mPwMenu = new PopupWindow(pwView);
        tvImport.setOnClickListener(v -> {
            // TODO: 2019/9/4 导入 PDF 逻辑
            SelectActivity.start(this, SELECT_REQUEST_CODE);
            mPwMenu.dismiss();
        });
//        tvSettings.setOnClickListener(v -> {
//            mPwMenu.dismiss();
//        });
        tvAbout.setOnClickListener(v -> {
            // TODO: 2019/9/4 App 关于逻辑
            mPwMenu.dismiss();
        });
        mPwMenu.setAnimationStyle(R.style.AppPwMenu);
        mPwMenu.setFocusable(true);
        mPwMenu.setOutsideTouchable(true);
        mPwMenu.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMenu.setElevation(ConvertUtils.dp2px(2));
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
            hideBlackCover();
        });

        mPwCollection.setAnimationStyle(R.style.AppPwCollection);
        mPwCollection.setFocusable(true);
        mPwCollection.setOutsideTouchable(true);
        mPwCollection.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPwCollection.setHeight(ConvertUtils.dp2px(490));
        mPwCollection.setElevation(ConvertUtils.dp2px(2));
    }

    private void applyBlur() {
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        // 获取当前窗口快照，相当于截屏
        Bitmap bitmap = view.getDrawingCache();
        blur(bitmap, findViewById(android.R.id.content));
    }

    private void blur(Bitmap bkg, View view) {
        long startMs = System.currentTimeMillis();
        float scaleFactor = 8; // 图片缩放比例；
        float radius = 20; // 模糊程度

        Bitmap overlay = Bitmap.createBitmap(
                (int) (view.getMeasuredWidth() / scaleFactor),
                (int) (view.getMeasuredHeight() / scaleFactor),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);


        overlay = BlurUtils.handleBlur(overlay, (int) radius, true);
        view.setBackground(new BitmapDrawable(getResources(), overlay));
        // 打印高斯模糊处理时间，如果时间大约16ms，用户就能感到到卡顿，时间越长卡顿越明显，如果对模糊完图片要求不高，可是将scaleFactor设置大一些。
        LogUtils.e("blur time:" + (System.currentTimeMillis() - startMs));
    }

    private void showBlackCover() {
        mBlackCover.setVisibility(View.VISIBLE);
        AlphaAnimation aa = new AlphaAnimation(0, 1);
        aa.setFillAfter(true);
        aa.setDuration(200);
        mBlackCover.startAnimation(aa);
    }

    private void hideBlackCover() {
        mBlackCover.setVisibility(View.GONE);
        AlphaAnimation aa = new AlphaAnimation(1, 0);
        aa.setFillAfter(true);
        aa.setDuration(200);
        mBlackCover.startAnimation(aa);
    }
}
