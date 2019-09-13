package com.aaron.yespdf.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
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
import android.view.View;
import android.view.ViewGroup;
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
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.CommonActivity;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.PdfUtils;
import com.aaron.yespdf.common.Settings;
import com.aaron.yespdf.common.UiManager;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.event.RecentPDFEvent;
import com.aaron.yespdf.settings.SettingsActivity;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.UriUtils;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.reflect.TypeToken;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.util.SizeF;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 注意点：显示到界面上的页数需要加 1 ，因为 PDFView 获取到的页数是从 0 计数的。
 */
public class PreviewActivity extends CommonActivity implements IActivityComm {

    private static final String EXTRA_PDF = "EXTRA_PDF";
    private static final int REQUEST_CODE_SETTINGS = 101;

    private static final float PREVIOUS = ScreenUtils.getScreenWidth() * 0.3F;
    private static final float NEXT = ScreenUtils.getScreenWidth() * 0.7F;

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
    @BindView(R.id.app_tv_content) TextView mTvContent;
    @BindView(R.id.app_tv_read_method) TextView mTvReadMethod;
    @BindView(R.id.app_tv_bookmark) TextView mTvBookmark;
    @BindView(R.id.app_tv_settings) TextView mTvSettings;

    // 左上角页码
    @BindView(R2.id.app_tv_pageinfo) TextView mTvPageinfo;

    // 目录书签页
    @BindView(R2.id.app_ll_content) ViewGroup mVgContent;
    @BindView(R2.id.app_tab_layout) TabLayout mTabLayout;
    @BindView(R2.id.app_vp) ViewPager mVp;

    @BindView(R.id.app_ll_read_method) LinearLayout mLlReadMethod;
    @BindView(R.id.app_tv_horizontal) TextView mTvHorizontal;
    @BindView(R.id.app_tv_vertical) TextView mTvVertical;

    private PDF mPDF; // 本应用打开
    private Uri mUri; // 一般是外部应用打开
    private int mPageCount;

    private IContetnFragComm mIContentFragComm;
    private IBkFragComm mIBkFragComm;

    private Map<Long, PdfDocument.Bookmark> mContentMap = new HashMap<>();
    private List<Long> mPageList = new ArrayList<>();
    private Map<Long, Bookmark> mBookmarkMap = new HashMap<>();

    // 记录 redo/undo的页码
    private int mPreviousPage;
    private int mNextPage;

    private Canvas mCanvas; // AndroidPDFView 的画布
    private Paint mPaint; // 画书签的画笔
    private float mPageWidth;

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
        enterFullScreen();
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
        }
        EventBus.getDefault().post(new RecentPDFEvent(true));
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideBar();
        mVgContent.setTranslationX(-mVgContent.getMeasuredWidth());
        mScreenCover.setAlpha(0);
        mLlReadMethod.setTranslationY(ScreenUtils.getScreenHeight());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPDFView.recycle();
    }

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
            closeContent();
        } else if (mLlReadMethod.getTranslationY() != ScreenUtils.getScreenHeight()) {
            closeReadMethod();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Settings.isVolumeControl() && mToolbar.getAlpha() == 0.0F) {
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
        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK) {
            initPdf(mUri, mPDF, mPageCount);
        }
    }

    @Override
    public void onJumpTo(int page) {
        mPDFViewBg.setVisibility(View.VISIBLE);
        mVgContent.animate()
                .setDuration(250)
                .translationX(-mVgContent.getMeasuredWidth())
                .setUpdateListener(valueAnimator -> {
                    mScreenCover.setAlpha(1 - valueAnimator.getAnimatedFraction());
                })
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mPDFView.jumpTo(page);
                    }
                })
                .start();
    }

    @SuppressLint({"SwitchIntDef"})
    private void initView() {
        UiManager.setTransparentStatusBar(this, mToolbar);
        UiManager.setNavigationBarColor(this, R.color.base_black);
        if (Settings.isNightMode()) {
            mPDFViewBg.setBackground(new ColorDrawable(Color.BLACK));
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.app_ic_action_back_white);
        }

        mLlReadMethod.setTranslationY(ScreenUtils.getScreenHeight());
        mVgContent.setTranslationX(-(ScreenUtils.getScreenWidth() - ConvertUtils.dp2px(64)));

        // 目录书签侧滑页初始化
        FragmentManager fm = getSupportFragmentManager();
        FragmentPagerAdapter adapter = new PagerAdapter(fm);
        mVp.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mVp);
        TabLayout.Tab tab1 = mTabLayout.getTabAt(0);
        TabLayout.Tab tab2 = mTabLayout.getTabAt(1);
        tab1.setCustomView(R.layout.app_tab_content);
        tab2.setCustomView(R.layout.app_tab_bookmark);

        setListener();

        Intent intent = getIntent();
        mUri = intent.getData();
        PDF pdf = intent.getParcelableExtra(EXTRA_PDF);
        if (mUri == null) {
            mPageCount = pdf != null ? pdf.getTotalPage() : 0;
        } else {
            File file = UriUtils.uri2File(mUri);
            mPageCount = file != null ? PdfUtils.getPdfTotalPage(file.getAbsolutePath()) : 0;
        }

        initPdf(mUri, pdf, mPageCount);
        enterFullScreen();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListener() {
        mIbtnQuickbarAction.setOnClickListener(v -> {
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
            mPDFViewBg.setVisibility(View.VISIBLE);
            if (mLlQuickBar.getVisibility() != View.VISIBLE) {
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
            mIbtnQuickbarAction.setSelected(false);
            int targetPage = mPDFView.getCurrentPage() - 1;
            while (!mPageList.contains((long) targetPage)) {
                if (targetPage < mPageList.get(0)) {
                    return;
                }
                targetPage--;
            }
            mPreviousPage = mPDFView.getCurrentPage();
            mPDFView.jumpTo(targetPage);
        });
        mTvNextChapter.setOnClickListener(v -> {
            mPDFViewBg.setVisibility(View.VISIBLE);
            if (mLlQuickBar.getVisibility() != View.VISIBLE) {
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
            mIbtnQuickbarAction.setSelected(false);
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
                mTabLayout.getTabAt(0).select();
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
            mTabLayout.getTabAt(1).select();
            hideBar();
            enterFullScreen();
            openContent();
            return true;
        });
        mTvSettings.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                hideBar();
                SettingsActivity.start(PreviewActivity.this, REQUEST_CODE_SETTINGS);
            }
        });
        mScreenCover.setOnTouchListener((view, event) -> {
            if (mVgContent.getTranslationX() == 0) {
                closeContent();
                return true;
            }
            return false;
        });
        mTvHorizontal.setOnClickListener(view -> {
            Settings.setSwipeHorizontal(true);
            initPdf(mUri, mPDF, mPageCount);
            closeReadMethod();
        });
        mTvVertical.setOnClickListener(view -> {
            Settings.setSwipeHorizontal(false);
            initPdf(mUri, mPDF, mPageCount);
            closeReadMethod();
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
    private void initPdf(Uri uri, PDF pdf, int pageCount) {
        mSbProgress.setMax(pageCount - 1);
        mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mTvPageinfo.setText((i + 1) + " / " + pageCount);

                // Quickbar
                mTvQuickbarTitle.setText(getTitle(i));
                mTvQuickbarPageinfo.setText((i + 1) + " / " + pageCount);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPDFViewBg.setVisibility(View.VISIBLE);
                mIbtnQuickbarAction.setSelected(false);
                mPreviousPage = seekBar.getProgress();

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

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mNextPage = seekBar.getProgress();
                mPDFView.jumpTo(seekBar.getProgress());
            }
        });
        PDFView.Configurator configurator;
        if (uri != null) {
            File file = UriUtils.uri2File(uri);
            String path = file != null ? UriUtils.uri2File(uri).getAbsolutePath() : "";
            mTvPageinfo.setText("1 / " + pageCount);
            String bookName = path.substring(path.lastIndexOf("/") + 1, path.length() - 4);
            mToolbar.post(() -> mToolbar.setTitle(bookName));
            configurator = mPDFView.fromUri(uri);
        } else if (pdf != null) {
            mPDF = pdf;
            String bkJson = pdf.getBookmark(); // 获取书签 json
            List<Bookmark> bkList = GsonUtils.fromJson(bkJson, new TypeToken<List<Bookmark>>(){}.getType());
            if (bkList != null) {
                for (Bookmark bk : bkList) {
                    mBookmarkMap.put((long) bk.getPageId(), bk);
                }
            }
            int curPage = pdf.getCurPage(); // 因为从0计数
            mSbProgress.setProgress(curPage);
            mTvPageinfo.setText((curPage + 1) + " / " + pageCount);
            mToolbar.post(() -> mToolbar.setTitle(pdf.getName()));
            configurator = mPDFView.fromFile(new File(pdf.getPath())).defaultPage(curPage);
        } else {
            return;
        }

        mPaint = new Paint();

        configurator.disableLongpress()
                .swipeHorizontal(Settings.isSwipeHorizontal())
                .nightMode(Settings.isNightMode())
                .pageFling(Settings.isSwipeHorizontal())
                .pageSnap(Settings.isSwipeHorizontal())
                .enableDoubletap(false)
                .fitEachPage(true)
//                .spacing(ConvertUtils.dp2px(4))
                .onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        // TODO: 2019/9/11 要提示
                        LogUtils.e(t.getMessage());
                    }
                })
                .onPageError(new OnPageErrorListener() {
                    @Override
                    public void onPageError(int page, Throwable t) {
                        // TODO: 2019/9/11 要提示
                        LogUtils.e(t.getMessage());
                    }
                })
                .onDrawAll(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                        mCanvas = canvas;
                        mPageWidth = pageWidth;
                        if (mBookmarkMap.containsKey((long) displayedPage)) {
                            drawBookmark(canvas, pageWidth);
                        }
                    }
                })
                .onPageChange((page, pageCount1) -> {
                    mTvBookmark.setSelected(mBookmarkMap.containsKey((long) page)); // 如果是书签则标红

                    mTvQuickbarTitle.setText(getTitle(page));
                    mTvQuickbarPageinfo.setText((page + 1) + " / " + pageCount);

                    mTvPageinfo.setText((page + 1) + " / " + pageCount);
                    mSbProgress.setProgress(page);
                })
                .onLoad(nbPages -> {
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
                    if (mLlReadMethod.getTranslationY() != ScreenUtils.getScreenHeight()) {
                        closeReadMethod();
                        return true;
                    }
                    float x = event.getRawX();
                    if (x <= PREVIOUS) {
                        if (mToolbar.getAlpha() == 1.0F) {
                            hideBar();
                            enterFullScreen();
                        } else {
                            int currentPage = mPDFView.getCurrentPage();
                            mPDFView.jumpTo(--currentPage, true);
                        }
                    } else if (x >= NEXT) {
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

    private void drawBookmark(Canvas canvas, float pageWidth) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.app_img_bookmark);
        float left = pageWidth - ConvertUtils.dp2px(36);
        canvas.drawBitmap(bitmap, left, 0, mPaint);
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

    private void closeContent() {
        mVgContent.animate()
                .setDuration(250)
                .translationX(-mVgContent.getMeasuredWidth())
                .setUpdateListener(valueAnimator -> {
                    mScreenCover.setAlpha(1 - valueAnimator.getAnimatedFraction());
                })
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
    }
}
