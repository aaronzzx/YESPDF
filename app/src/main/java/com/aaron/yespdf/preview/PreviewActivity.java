package com.aaron.yespdf.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.aaron.base.impl.OnClickListenerImpl;
import com.aaron.base.impl.TextWatcherImpl;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.CommonActivity;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.Settings;
import com.aaron.yespdf.common.UiManager;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.event.RecentPDFEvent;
import com.aaron.yespdf.common.utils.DialogUtils;
import com.aaron.yespdf.settings.SettingsActivity;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.UriUtils;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.reflect.TypeToken;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfPasswordException;
import com.shockwave.pdfium.util.SizeF;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 注意点：显示到界面上的页数需要加 1 ，因为 PDFView 获取到的页数是从 0 计数的。
 */
public class PreviewActivity extends CommonActivity implements IActivityComm {

    private static final String EXTRA_PDF = "EXTRA_PDF";
    private static final int REQUEST_CODE_SETTINGS = 101;

    private static final float OFFSET_Y = -0.5F; // 自动滚动的偏离值

    @BindView(R2.id.app_screen_cover) View mScreenCover; // 遮罩

    // PDF 阅读器
    @BindView(R2.id.app_pdfview_bg) View mPDFViewBg;
    @BindView(R2.id.app_pdfview) PDFView mPDFView;

    // 快速撤销栏
    @BindView(R2.id.app_ll_undoredobar) LinearLayout mLlQuickBar;
    @BindView(R2.id.app_quickbar_title) TextView mTvQuickbarTitle;
    @BindView(R2.id.app_tv_pageinfo2) TextView mTvQuickbarPageinfo;
    @BindView(R2.id.app_ibtn_quickbar_action) ImageButton mIbtnQuickbarAction;

    // 底栏
    @BindView(R2.id.app_ll_bottombar) LinearLayout mLlBottomBar;
    @BindView(R2.id.app_tv_previous_chapter) TextView mTvPreviousChapter;
    @BindView(R.id.app_sb_progress) SeekBar mSbProgress;
    @BindView(R2.id.app_tv_next_chapter) TextView mTvNextChapter;
    @BindView(R2.id.app_tv_content)
    TextView mTvContent;
    @BindView(R2.id.app_tv_read_method)
    TextView mTvReadMethod;
    @BindView(R2.id.app_tv_auto_scroll)
    TextView mTvAutoScroll;
    @BindView(R2.id.app_tv_bookmark)
    TextView mTvBookmark;
    @BindView(R2.id.app_tv_settings)
    TextView mTvSettings;

    // 左上角页码
    @BindView(R2.id.app_tv_pageinfo) TextView mTvPageinfo;

    // 弹出式目录书签页
    @BindView(R2.id.app_ll_content) ViewGroup mVgContent;
    @BindView(R2.id.app_tab_layout) TabLayout mTabLayout;
    @BindView(R2.id.app_vp) ViewPager mVp;

    // 弹出式阅读方式
    @BindView(R.id.app_ll_read_method) LinearLayout mLlReadMethod;
    @BindView(R.id.app_tv_horizontal) TextView mTvHorizontal;
    @BindView(R.id.app_tv_vertical) TextView mTvVertical;

    private PDF mPDF; // 本应用打开
    private Uri mUri; // 一般是外部应用打开
    private int mDefaultPage;
    private int mPageCount;
    private String mPassword;

    private boolean isNightMode = Settings.isNightMode();
    private boolean isVolumeControl = Settings.isVolumeControl();

    private IContetnFragComm mIContentFragComm;
    private IBkFragComm mIBkFragComm;
    private Disposable mAutoDisp; // 自动滚动
    private boolean isPause;

    private Map<Long, PdfDocument.Bookmark> mContentMap = new HashMap<>();
    private Map<Long, Bookmark> mBookmarkMap = new HashMap<>();
    private List<Long> mPageList = new ArrayList<>();

    // 记录 redo/undo的页码
    private int mPreviousPage;
    private int mNextPage;

    private Canvas mCanvas; // AndroidPDFView 的画布
    private Paint mPaint; // 画书签的画笔
    private float mPageWidth;

    /**
     * 非外部文件打开
     */
    public static void start(Context context, PDF pdf) {
        Intent starter = new Intent(context, PreviewActivity.class);
        starter.putExtra(EXTRA_PDF, pdf);
        context.startActivity(starter);
    }

    @Override
    protected int layoutId() {
        return R.layout.app_activity_preview;
    }

    @Override
    protected Toolbar createToolbar() {
        return findViewById(R.id.app_toolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        enterFullScreen(); // 重新回到界面时主动进入全屏
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPDF != null) {
            int curPage = mPDFView.getCurrentPage();
            int pageCount = mPDFView.getPageCount();
            String progress = getPercent(curPage + 1, pageCount);
            mPDF.setCurPage(curPage);
            mPDF.setProgress(progress);
            mPDF.setBookmark(GsonUtils.toJson(mBookmarkMap.values()));
            DBHelper.updatePDF(mPDF);
            // 这里发出事件主要是更新界面阅读进度
            EventBus.getDefault().post(new RecentPDFEvent(true));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideBar();
        // 书签页回原位
        mVgContent.setTranslationX(-mVgContent.getMeasuredWidth());
        mScreenCover.setAlpha(0); // 隐藏界面遮罩
        // 阅读方式回原位
        mLlReadMethod.setTranslationY(ScreenUtils.getScreenHeight());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPDFView.recycle();
    }

    /**
     * 获取阅读进度的百分比
     */
    private String getPercent(int percent, int total) {
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(1);
        //计算x年x月的成功率
        String result = numberFormat.format((float) percent / (float) total * 100);
        return result + "%";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mVgContent.getTranslationX() == 0) {
            // 等于 0 表示正处于打开状态，需要隐藏
            closeContent(null);
        } else if (mLlReadMethod.getTranslationY() != ScreenUtils.getScreenHeight()) {
            // 不等于屏幕高度表示正处于显示状态，需要隐藏
            closeReadMethod();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAutoDisp != null && !mAutoDisp.isDisposed()) {
            exitFullScreen();
            showBar();
            return true;
        }
        // 如果非全屏状态是无法使用音量键翻页的
        if (ScreenUtils.isPortrait() && isVolumeControl && mToolbar.getAlpha() == 0.0F) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    int currentPage1 = mPDFView.getCurrentPage();
                    mPDFView.jumpTo(--currentPage1, true);
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    int currentPage2 = mPDFView.getCurrentPage();
                    mPDFView.jumpTo(++currentPage2, true);
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETTINGS) {
            isVolumeControl = Settings.isVolumeControl();
            if (isNightMode != Settings.isNightMode()) {
                isNightMode = Settings.isNightMode();
                initPdf(mUri, mPDF);
            }
        }
    }

    @Override
    public void onJumpTo(int page) {
        mPDFViewBg.setVisibility(View.VISIBLE);
        closeContent(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mPDFView.jumpTo(page);
            }
        });
    }

    @SuppressLint({"SwitchIntDef"})
    private void initView() {
        if (isNightMode) {
            mPDFViewBg.setBackground(new ColorDrawable(Color.BLACK));
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.app_ic_action_back_white);
        }

        if (!Settings.isSwipeHorizontal()) {
            ViewGroup.LayoutParams lp = mPDFViewBg.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mPDFViewBg.setLayoutParams(lp);
        }

        // 移动到屏幕下方
        mLlReadMethod.setTranslationY(ScreenUtils.getScreenHeight());
        // 移动到屏幕左边
        mVgContent.post(() -> mVgContent.setTranslationX(-mVgContent.getMeasuredWidth()));

        if (Settings.isSwipeHorizontal()) {
            mTvHorizontal.setTextColor(getResources().getColor(R.color.app_color_accent));
        } else {
            mTvVertical.setTextColor(getResources().getColor(R.color.app_color_accent));
        }

        // 目录书签侧滑页初始化
        FragmentManager fm = getSupportFragmentManager();
        FragmentPagerAdapter adapter = new PagerAdapter(fm);
        mVp.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mVp);
        TabLayout.Tab tab1 = mTabLayout.getTabAt(0);
        TabLayout.Tab tab2 = mTabLayout.getTabAt(1);
        if (tab1 != null) tab1.setCustomView(R.layout.app_tab_content);
        if (tab2 != null) tab2.setCustomView(R.layout.app_tab_bookmark);

        getData();

        setListener();

        initPdf(mUri, mPDF);

        enterFullScreen();
    }

    private void getData() {
        Intent intent = getIntent();
        mUri = intent.getData();
        mPDF = intent.getParcelableExtra(EXTRA_PDF);
        if (mPDF != null) {
            mDefaultPage = mPDF.getCurPage();
            mPageCount = mPDF.getTotalPage();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListener() {
        mIbtnQuickbarAction.setOnClickListener(v -> {
            // 当前页就是操作后的上一页或者下一页
            if (v.isSelected()) {
                mPreviousPage = mPDFView.getCurrentPage();
                mPDFView.jumpTo(mNextPage); // Redo
            } else {
                mNextPage = mPDFView.getCurrentPage();
                mPDFView.jumpTo(mPreviousPage); // Undo
            }
            v.setSelected(!v.isSelected());
        });
        mTvPreviousChapter.setOnClickListener(v -> {
            if (mLlQuickBar.getVisibility() != View.VISIBLE) {
                showQuickbar();
            }
            mIbtnQuickbarAction.setSelected(false); // 将状态调为 Undo
            // 减 1 是为了防止当前页面有标题的情况下无法跳转，因为是按标题来跳转
            int targetPage = mPDFView.getCurrentPage() - 1;
            while (!mPageList.contains((long) targetPage)) {
                if (targetPage < mPageList.get(0)) {
                    return; // 如果实在匹配不到就跳出方法，不执行跳转
                }
                targetPage--; // 如果匹配不到会一直减 1 搜索
            }
            mPreviousPage = mPDFView.getCurrentPage();
            mPDFView.jumpTo(targetPage);
        });
        mTvNextChapter.setOnClickListener(v -> {
            mPDFViewBg.setVisibility(View.VISIBLE);
            if (mLlQuickBar.getVisibility() != View.VISIBLE) {
                showQuickbar();
            }
            mIbtnQuickbarAction.setSelected(false);
            // 这里的原理和上面跳转上一章节一样
            int targetPage = mPDFView.getCurrentPage() + 1;
            while (!mPageList.contains((long) targetPage)) {
                if (targetPage > mPageList.get(mPageList.size() - 1)) {
                    return;
                }
                targetPage++;
            }
            mPreviousPage = mPDFView.getCurrentPage();
            mPDFView.jumpTo(targetPage);
        });
        mTvContent.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                TabLayout.Tab tab = mTabLayout.getTabAt(0);
                if (tab != null) tab.select();
                hideBar();
                enterFullScreen();
                openContent();
            }
        });
        mTvReadMethod.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                hideBar();
                enterFullScreen();
                openReadMethod();
            }
        });
        mTvAutoScroll.setOnClickListener(v -> {
            if (Settings.isSwipeHorizontal()) {
                UiManager.showShort(R.string.app_horizontal_does_not_support_auto_scroll);
                return;
            }
            v.setSelected(!v.isSelected());
            if (v.isSelected()) {
                hideBar();
                enterFullScreen();
                mAutoDisp = Observable.interval(Settings.getScrollLevel(), TimeUnit.MILLISECONDS)
                        .doOnDispose(() -> {
                            mTvAutoScroll.setSelected(false);
                            isPause = false;
                        })
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(PreviewActivity.this)))
                        .subscribe(aLong -> {
                            if (!mPDFView.isRecycled() && !isPause) {
                                mPDFView.moveRelativeTo(0, OFFSET_Y);
                                mPDFView.loadPageByOffset();
                            }
                        });
            } else {
                if (!mAutoDisp.isDisposed()) mAutoDisp.dispose();
            }
        });
        mTvBookmark.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            int curPage = mPDFView.getCurrentPage();
            if (v.isSelected()) {
                drawBookmark(mCanvas, mPageWidth);
                String title = getTitle(curPage);
                long time = System.currentTimeMillis();
                Bookmark bk = new Bookmark(curPage, title, time);
                mBookmarkMap.put((long) curPage, bk);
            } else {
                mBookmarkMap.remove((long) curPage);
            }
            mIBkFragComm.update(mBookmarkMap.values());
            mPDFView.invalidate();
        });
        mTvBookmark.setOnLongClickListener(view -> {
            TabLayout.Tab tab = mTabLayout.getTabAt(1);
            if (tab != null) tab.select();
            hideBar();
            enterFullScreen();
            openContent();
            return true;
        });
        mTvSettings.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                hideBar();
                if (mAutoDisp != null && !mAutoDisp.isDisposed()) {
                    mAutoDisp.dispose();
                }
                SettingsActivity.start(PreviewActivity.this, REQUEST_CODE_SETTINGS);
            }
        });
        mScreenCover.setOnTouchListener((view, event) -> {
            if (mVgContent.getTranslationX() == 0) {
                closeContent(null);
                return true;
            }
            return false;
        });
        mTvHorizontal.setOnClickListener(view -> {
            closeReadMethod();
            if (!Settings.isSwipeHorizontal()) {
                if (mAutoDisp != null && !mAutoDisp.isDisposed()) {
                    mAutoDisp.dispose();
                    UiManager.showShort(R.string.app_horizontal_does_not_support_auto_scroll);
                    return;
                }
                mTvHorizontal.setTextColor(getResources().getColor(R.color.app_color_accent));
                mTvVertical.setTextColor(getResources().getColor(R.color.base_white));
                Settings.setSwipeHorizontal(true);
                initPdf(mUri, mPDF);
            }
        });
        mTvVertical.setOnClickListener(view -> {
            closeReadMethod();
            if (Settings.isSwipeHorizontal()) {
                mTvVertical.setTextColor(getResources().getColor(R.color.app_color_accent));
                mTvHorizontal.setTextColor(getResources().getColor(R.color.base_white));
                Settings.setSwipeHorizontal(false);
                initPdf(mUri, mPDF);
            }
        });
    }

    private void openReadMethod() {
        int screenHeight = ScreenUtils.getScreenHeight();
        mLlReadMethod.animate()
                .setDuration(200)
                .setStartDelay(100)
                .translationY(screenHeight - mLlReadMethod.getMeasuredHeight())
                .start();
    }

    private void closeReadMethod() {
        int screenHeight = ScreenUtils.getScreenHeight();
        mLlReadMethod.animate()
                .setDuration(200)
                .translationY(screenHeight)
                .start();
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initPdf(Uri uri, PDF pdf) {
        mSbProgress.setMax(mPageCount - 1);
        mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mTvPageinfo.setText((i + 1) + " / " + mPageCount);
                // Quickbar
                mTvQuickbarTitle.setText(getTitle(i));
                mTvQuickbarPageinfo.setText((i + 1) + " / " + mPageCount);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIbtnQuickbarAction.setSelected(false);
                mPreviousPage = seekBar.getProgress();

                showQuickbar();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mNextPage = seekBar.getProgress();
                mPDFView.jumpTo(seekBar.getProgress());
            }
        });
        PDFView.Configurator configurator;
        if (uri != null) {
            File file = UriUtils.uri2File(uri);
            String path = file != null ? UriUtils.uri2File(uri).getAbsolutePath() : null;
            mTvPageinfo.setText("1 / " + mPageCount);
            String bookName = path != null ? path.substring(path.lastIndexOf("/") + 1, path.length() - 4) : "";
            mToolbar.post(() -> mToolbar.setTitle(bookName));
            configurator = mPDFView.fromUri(uri).defaultPage(mDefaultPage);
        } else if (pdf != null) {
            String bkJson = pdf.getBookmark(); // 获取书签 json
            List<Bookmark> bkList = GsonUtils.fromJson(bkJson, new TypeToken<List<Bookmark>>(){}.getType());
            if (bkList != null) {
                for (Bookmark bk : bkList) {
                    mBookmarkMap.put((long) bk.getPageId(), bk);
                }
            }
            mSbProgress.setProgress(mDefaultPage);
            mTvPageinfo.setText((mDefaultPage + 1) + " / " + mPageCount);
            mToolbar.post(() -> mToolbar.setTitle(pdf.getName()));
            configurator = mPDFView.fromFile(new File(pdf.getPath())).defaultPage(mDefaultPage);
        } else {
            UiManager.showShort(R.string.app_file_is_empty);
            finish();
            return;
        }
        if (mPassword != null) configurator = configurator.password(mPassword);

        mPaint = new Paint();

        configurator.disableLongpress()
                .swipeHorizontal(Settings.isSwipeHorizontal())
                .nightMode(isNightMode)
                .pageFling(Settings.isSwipeHorizontal())
                .pageSnap(Settings.isSwipeHorizontal())
                .enableDoubletap(false)
                .fitEachPage(true)
//                .spacing(ConvertUtils.dp2px(4))
                .onError(throwable -> {
                    LogUtils.e(throwable.getMessage());
                    if (throwable instanceof PdfPasswordException) {
                        if (!StringUtils.isEmpty(mPassword)) {
                            UiManager.showShort(R.string.app_password_error);
                        }
                        showInputDialog();
                    } else {
                        showAlertDialog();
                    }
                })
                .onPageError((page, throwable) -> {
                    LogUtils.e(throwable.getMessage());
                    UiManager.showShort(getString(R.string.app_cur_page) + page + getString(R.string.app_parse_error));
                })
                .onDrawAll((canvas, pageWidth, pageHeight, displayedPage) -> {
                    mCanvas = canvas;
                    mPageWidth = pageWidth;
                    if (mBookmarkMap.containsKey((long) displayedPage)) {
                        drawBookmark(canvas, pageWidth);
                    }
                })
                .onPageChange((page, pageCount) -> {
                    mDefaultPage = page;
                    mTvBookmark.setSelected(mBookmarkMap.containsKey((long) page)); // 如果是书签则标红

                    mTvQuickbarTitle.setText(getTitle(page));
                    mTvQuickbarPageinfo.setText((page + 1) + " / " + mPageCount);

                    mTvPageinfo.setText((page + 1) + " / " + mPageCount);
                    mSbProgress.setProgress(page);
                })
                .onLoad(nbPages -> {
                    mPageCount = mPDFView.getPageCount();
                    mSbProgress.setMax(mPageCount);
                    List<PdfDocument.Bookmark> list = mPDFView.getTableOfContents();
                    findContent(list);

                    List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
                    for (Fragment f : fragmentList) {
                        if (f instanceof IContetnFragComm) {
                            mIContentFragComm = (IContetnFragComm) f;
                        } else if (f instanceof IBkFragComm) {
                            mIBkFragComm = (IBkFragComm) f;
                        }
                    }
                    mIContentFragComm.update(list);
                    mIBkFragComm.update(mBookmarkMap.values());

                    Set<Long> keySet = mContentMap.keySet();
                    mPageList.addAll(keySet);
                    Collections.sort(mPageList);

                    if (!Settings.isNightMode()) {
                        mPDFViewBg.setBackground(new ColorDrawable(Color.WHITE));
                        if (Settings.isSwipeHorizontal()) {
                            int page = Math.round(mPDFView.getPageCount() / 2);
                            SizeF sizeF = mPDFView.getPageSize(page);
                            ViewGroup.LayoutParams lp = mPDFViewBg.getLayoutParams();
                            lp.width = (int) sizeF.getWidth();
                            lp.height = (int) sizeF.getHeight();
                            mPDFViewBg.setLayoutParams(lp);
                        } else {
                            ViewGroup.LayoutParams lp = mPDFViewBg.getLayoutParams();
                            lp.width = ScreenUtils.getScreenWidth();
                            lp.height = ScreenUtils.getScreenHeight();
                            mPDFViewBg.setLayoutParams(lp);
                        }
                    } else {
                        mPDFViewBg.setBackground(new ColorDrawable(Color.BLACK));
                    }
                })
                .onTap(event -> {
                    if (mAutoDisp != null && !mAutoDisp.isDisposed()) {
                        if (mToolbar.getAlpha() == 1.0F) {
                            hideBar();
                            enterFullScreen();
                        } else if (mLlReadMethod.getTranslationY() != ScreenUtils.getScreenHeight()) {
                            closeReadMethod();
                        } else {
                            isPause = !isPause;
                        }
                        return true;
                    }
                    if (mLlReadMethod.getTranslationY() != ScreenUtils.getScreenHeight()) {
                        closeReadMethod();
                        return true;
                    }
                    float x = event.getRawX();

                    float previous = ScreenUtils.getScreenWidth() * 0.3F;
                    float next = ScreenUtils.getScreenWidth() * 0.7F;
                    if (ScreenUtils.isPortrait() && x <= previous) {
                        if (mToolbar.getAlpha() == 1.0F) {
                            hideBar();
                            enterFullScreen();
                        } else {
                            int currentPage = mPDFView.getCurrentPage();
                            mPDFView.jumpTo(--currentPage, true);
                        }
                    } else if (ScreenUtils.isPortrait() && x >= next) {
                        if (mToolbar.getAlpha() == 1.0F) {
                            hideBar();
                            enterFullScreen();
                        } else {
                            int currentPage = mPDFView.getCurrentPage();
                            mPDFView.jumpTo(++currentPage, true);
                        }
                    } else {
                        boolean visible = mToolbar.getAlpha() == 1.0F
                                && mLlBottomBar.getAlpha() == 1.0F;
                        if (visible) {
                            hideBar();
                            enterFullScreen();
                        } else {
                            exitFullScreen();
                            showBar();
                        }
                    }
                    return true;
                })
                .load();
    }

    private void showInputDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.app_dialog_input, null);
        TextView tvTitle = view.findViewById(R.id.app_tv_title);
        EditText etInput = view.findViewById(R.id.app_et_input);
        Button btnCancel = view.findViewById(R.id.app_btn_cancel);
        Button btnConfirm = view.findViewById(R.id.app_btn_confirm);
        Dialog dialog = DialogUtils.createDialog(this, view);
        tvTitle.setText(R.string.app_need_verify_password);
        btnCancel.setText(R.string.app_cancel);
        btnConfirm.setText(R.string.app_confirm);
        btnCancel.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> {
            initPdf(mUri, mPDF);
            dialog.dismiss();
        });
        etInput.addTextChangedListener(new TextWatcherImpl() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mPassword = charSequence.toString();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void showAlertDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.app_dialog_alert, null);
        TextView tvTitle = view.findViewById(R.id.app_tv_title);
        TextView tvContent = view.findViewById(R.id.app_tv_content);
        Button btn = view.findViewById(R.id.app_btn);
        tvTitle.setText(R.string.app_oop_error);
        tvContent.setText(R.string.app_doc_parse_error);
        btn.setText(R.string.app_exit_cur_content);
        btn.setOnClickListener(v -> finish());
        Dialog dialog = DialogUtils.createDialog(this, view);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void drawBookmark(Canvas canvas, float pageWidth) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.app_img_bookmark);
        float left = pageWidth - ConvertUtils.dp2px(36);
        canvas.drawBitmap(bitmap, left, 0, mPaint);
    }

    private void showQuickbar() {
        mLlQuickBar.animate()
                .setDuration(50)
                .alpha(1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mLlQuickBar.setVisibility(View.VISIBLE);
                    }
                })
                .start();
    }

    private void openContent() {
        mVgContent.animate()
                .setDuration(250)
                .setStartDelay(100)
                .translationX(0)
                .setUpdateListener(valueAnimator -> {
                    mScreenCover.setAlpha(valueAnimator.getAnimatedFraction());
                })
                .start();
    }

    private void closeContent(Animator.AnimatorListener listener) {
        mVgContent.animate()
                .setDuration(250)
                .translationX(-mVgContent.getMeasuredWidth())
                .setUpdateListener(valueAnimator -> {
                    mScreenCover.setAlpha(1 - valueAnimator.getAnimatedFraction());
                })
                .setListener(listener)
                .start();
    }

    private void findContent(List<PdfDocument.Bookmark> list) {
        for (PdfDocument.Bookmark bk : list) {
            mContentMap.put(bk.getPageIdx(), bk);
            if (bk.hasChildren()) {
                findContent(bk.getChildren());
            }
        }
    }

    private String getTitle(int page) {
        if (mContentMap.isEmpty()) return getString(R.string.app_have_no_content);
        String title;
        PdfDocument.Bookmark bk = mContentMap.get((long) page);
        title = bk != null ? bk.getTitle() : null;
        if (StringUtils.isEmpty(title)) {
            if (page < mPageList.get(0)) {
                title = mContentMap.get(mPageList.get(0)).getTitle();
            } else {
                int index = mPageList.indexOf((long) page);
                while (index == -1) {
                    index = mPageList.indexOf((long) page--);
                }
                title = mContentMap.get(mPageList.get(index)).getTitle();
            }
        }
        return title;
    }

    private void showBar() {
        mToolbar.animate().setDuration(250).alpha(1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mToolbar.setVisibility(View.VISIBLE);
                    }
                }).start();
        mLlBottomBar.animate().setDuration(250).alpha(1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mLlBottomBar.setVisibility(View.VISIBLE);
                    }
                }).start();
        mTvPageinfo.animate().setDuration(250).alpha(1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mTvPageinfo.setVisibility(View.VISIBLE);
                    }
                }).start();
    }

    private void hideBar() {
        mToolbar.animate().setDuration(250).alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mToolbar.setVisibility(View.GONE);
                    }
                }).start();
        mLlBottomBar.animate().setDuration(250).alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLlBottomBar.setVisibility(View.GONE);
                    }
                }).start();
        mTvPageinfo.animate().setDuration(250).alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mTvPageinfo.setVisibility(View.GONE);
                    }
                }).start();
        mLlQuickBar.animate().setDuration(250).alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLlQuickBar.setVisibility(View.GONE);
                    }
                }).start();
        mIbtnQuickbarAction.setSelected(false); // 初始化为 Undo 状态
    }

    private void enterFullScreen() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void exitFullScreen() {

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mToolbar.post(() -> UiManager.setNavigationBarColor(this, getResources().getColor(R.color.base_black)));
    }
}
