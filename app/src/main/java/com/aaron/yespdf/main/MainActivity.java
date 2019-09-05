package com.aaron.yespdf.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.aaron.yespdf.BlurUtils;
import com.aaron.yespdf.CommonActivity;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.filepicker.SelectActivity;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends CommonActivity implements Communicable {

    //    @BindView(R2.id.app_et_search)  EditText mEtSearch;
//    @BindView(R2.id.app_ibtn_clear) ImageButton mIbtnClear;
    @BindView(R2.id.app_tab_layout) TabLayout mTabLayout;
    @BindView(R2.id.app_vp) ViewPager mVp;
    @BindView(R2.id.app_black_cover) View mBlackCover;

    private Unbinder mUnbinder;
    private PopupWindow mPwMenu;
    private PopupWindow mPwCollection;
    private RecyclerView mRvCollection;

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

    @Override
    public void onTap() {
        mPwCollection.showAtLocation(mVp, Gravity.CENTER, 0, 0);
        showBlackCover();
//        applyBlur();
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.alpha = 0.3f;
//        getWindow().setAttributes(lp);
    }

    private void initView(Bundle savedInstanceState) {
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
        mPwMenu.setAnimationStyle(R.style.AppPwMenu);
        mPwMenu.setFocusable(true);
        mPwMenu.setOutsideTouchable(true);
        mPwMenu.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMenu.setElevation(ConvertUtils.dp2px(2));
    }

    private void initPwCollection() {
        View pwView = LayoutInflater.from(this).inflate(R.layout.app_pw_collection, null);
        mRvCollection = pwView.findViewById(R.id.app_rv_collection);
        mRvCollection.addItemDecoration(new XGridDecoration());
        mRvCollection.addItemDecoration(new YGridDecoration());
        RecyclerView.LayoutManager lm = new GridLayoutManager(this, 3);
        mRvCollection.setLayoutManager(lm);

        List<String> pathList = new ArrayList<>();
        pathList.add("/storage/emulated/0/Android#Java/Java/算法(第4版).pdf");
        pathList.add("/storage/emulated/0/Android#Java/Java/大话设计模式.pdf");
        pathList.add("/storage/emulated/0/Android#Java/Java/Effective Java 第二版 中文版.pdf");
        pathList.add("/storage/emulated/0/Android#Java/Java/Java 编程的逻辑.pdf");
        RecyclerView.Adapter adapter = new CollectionAdapter(pathList);
        mRvCollection.setAdapter(adapter);
        mPwCollection = new PopupWindow(pwView);
        mPwCollection.setOnDismissListener(() -> {
            mRvCollection.scrollToPosition(0);
            hideBlackCover();
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//            WindowManager.LayoutParams lp = getWindow().getAttributes();
//            lp.alpha = 1.0F;
//            getWindow().setAttributes(lp);
        });

        mPwCollection.setAnimationStyle(R.style.AppPwCollection);
        mPwCollection.setFocusable(true);
        mPwCollection.setOutsideTouchable(true);
        mPwCollection.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPwCollection.setHeight(ConvertUtils.dp2px(500));
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
