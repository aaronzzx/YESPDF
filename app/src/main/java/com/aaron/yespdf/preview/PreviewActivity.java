package com.aaron.yespdf.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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

import androidx.annotation.NonNull;
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
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 注意点：显示到界面上的页数需要加 1 ，因为 PDFView 获取到的页数是从 0 计数的。
 */
public class PreviewActivity extends CommonActivity implements IActivityInterface {

    private static final String EXTRA_PDF = "EXTRA_PDF";
    private static final String BUNDLE_CUR_PAGE = "BUNDLE_CUR_PAGE";

    private static final int REQUEST_CODE_SETTINGS = 101;

    private static final float OFFSET_Y = -0.5F; // 自动滚动的偏离值

    @BindView(R2.id.app_screen_cover)
    View screenCover; // 遮罩

    // PDF 阅读器
    @BindView(R2.id.app_pdfview_bg)
    View pdfViewBg;
    @BindView(R2.id.app_pdfview)
    PDFView pdfView;

    // 快速撤销栏
    @BindView(R2.id.app_ll_undoredobar)
    LinearLayout llQuickBar;
    @BindView(R2.id.app_quickbar_title)
    TextView tvQuickbarTitle;
    @BindView(R2.id.app_tv_pageinfo2)
    TextView tvQuickbarPageinfo;
    @BindView(R2.id.app_ibtn_quickbar_action)
    ImageButton ibtnQuickbarAction;

    // 底栏
    @BindView(R2.id.app_ll_bottombar)
    LinearLayout llBottomBar;
    @BindView(R2.id.app_tv_previous_chapter)
    TextView tvPreviousChapter;
    @BindView(R.id.app_sb_progress)
    SeekBar sbProgress;
    @BindView(R2.id.app_tv_next_chapter)
    TextView tvNextChapter;
    @BindView(R2.id.app_tv_content)
    TextView tvContent;
    @BindView(R2.id.app_tv_read_method)
    TextView tvReadMethod;
    @BindView(R2.id.app_tv_auto_scroll)
    TextView tvAutoScroll;
    @BindView(R2.id.app_tv_bookmark)
    TextView tvBookmark;
    @BindView(R2.id.app_tv_more)
    TextView tvMore;

    // 左上角页码
    @BindView(R2.id.app_tv_pageinfo)
    TextView tvPageinfo;

    // 弹出式目录书签页
    @BindView(R2.id.app_ll_content)
    ViewGroup vgContent;
    @BindView(R2.id.app_tab_layout)
    TabLayout tabLayout;
    @BindView(R2.id.app_vp)
    ViewPager vp;

    // 弹出式阅读方式
    @BindView(R.id.app_ll_read_method)
    LinearLayout llReadMethod;
    @BindView(R.id.app_tv_horizontal)
    TextView tvHorizontal;
    @BindView(R.id.app_tv_vertical)
    TextView tvVertical;

    // 弹出式更多设置
    @BindView(R.id.app_ll_more)
    ViewGroup vgMore;
    @BindView(R.id.app_tv_lock_landscape)
    TextView tvLockLandscape;
    @BindView(R.id.app_tv_settings)
    TextView tvSettings;

    private PDF pdf; // 本应用打开
    private Uri uri; // 一般是外部应用打开
    private int curPage;
    private int pageCount;
    private String password;

    private boolean isNightMode = Settings.isNightMode();
    private boolean isVolumeControl = Settings.isVolumeControl();

    private IContentFragInterface contentFragInterface;
    private IBkFragInterface bkFragInterface;
    private Disposable autoDisp; // 自动滚动
    private boolean isPause;

    private Map<Long, PdfDocument.Bookmark> contentMap = new HashMap<>();
    private Map<Long, Bookmark> bookmarkMap = new HashMap<>();
    private List<Long> pageList = new ArrayList<>();

    // 记录 redo/undo的页码
    private int previousPage;
    private int nextPage;

    private Unbinder unbinder;
    private Canvas canvas; // AndroidPDFView 的画布
    private Paint paint; // 画书签的画笔
    private float pageWidth;

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
        LogUtils.e("onCreate");
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        initView(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        LogUtils.e("onRestart");
        super.onRestart();
        enterFullScreen(); // 重新回到界面时主动进入全屏
    }

    @Override
    protected void onPause() {
        LogUtils.e("onPause");
        super.onPause();
        if (pdf != null) {
//            int curPage = pdfView.getCurrentPage();
//            int pageCount = pdfView.getPageCount();
            String progress = getPercent(curPage + 1, pageCount);
            pdf.setCurPage(curPage);
            LogUtils.e("curPage: " + curPage);
            pdf.setProgress(progress);
            pdf.setBookmark(GsonUtils.toJson(bookmarkMap.values()));
            DBHelper.updatePDF(pdf);
            // 这里发出事件主要是更新界面阅读进度
            EventBus.getDefault().post(new RecentPDFEvent(true));
        }
    }

    @Override
    protected void onStop() {
        LogUtils.e("onStop");
        super.onStop();
        hideBar();
        // 书签页回原位
        vgContent.setTranslationX(-vgContent.getMeasuredWidth());
        screenCover.setAlpha(0); // 隐藏界面遮罩
        // 阅读方式回原位
        llReadMethod.setTranslationY(ScreenUtils.getScreenHeight());
        vgMore.setTranslationY(ScreenUtils.getScreenHeight());
        if (autoDisp != null && !autoDisp.isDisposed()) autoDisp.dispose();
    }

    @Override
    protected void onDestroy() {
        LogUtils.e("onDestroy");
        super.onDestroy();
        pdfView.recycle();
        unbinder.unbind();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        LogUtils.e("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_CUR_PAGE, curPage);
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
        if (vgContent.getTranslationX() == 0) {
            // 等于 0 表示正处于打开状态，需要隐藏
            closeContent(null);
        } else if (llReadMethod.getTranslationY() != ScreenUtils.getScreenHeight()) {
            // 不等于屏幕高度表示正处于显示状态，需要隐藏
            closeReadMethod();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                && autoDisp != null && !autoDisp.isDisposed()) {
            exitFullScreen();
            showBar();
            return true;
        } else if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                && ScreenUtils.isPortrait() && isVolumeControl && mToolbar.getAlpha() == 0.0F) {
            // 如果非全屏状态是无法使用音量键翻页的
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    int currentPage1 = pdfView.getCurrentPage();
                    pdfView.jumpTo(--currentPage1, true);
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    int currentPage2 = pdfView.getCurrentPage();
                    pdfView.jumpTo(++currentPage2, true);
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
                initPdf(uri, pdf);
            }
        }
    }

    @Override
    public void onJumpTo(int page) {
        pdfViewBg.setVisibility(View.VISIBLE);
        closeContent(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                pdfView.jumpTo(page);
            }
        });
    }

    @SuppressLint({"SwitchIntDef"})
    private void initView(Bundle savedInstanceState) {
        if (isNightMode) {
            pdfViewBg.setBackground(new ColorDrawable(Color.BLACK));
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.app_ic_action_back_white);
        }

        if (!Settings.isSwipeHorizontal()) {
            ViewGroup.LayoutParams lp = pdfViewBg.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            pdfViewBg.setLayoutParams(lp);
        }

        // 移动到屏幕下方
        llReadMethod.setTranslationY(ScreenUtils.getScreenHeight());
        vgMore.setTranslationY(ScreenUtils.getScreenHeight());
        // 移动到屏幕左边
        vgContent.post(() -> vgContent.setTranslationX(-vgContent.getMeasuredWidth()));

        if (Settings.isSwipeHorizontal()) {
            tvHorizontal.setTextColor(getResources().getColor(R.color.app_color_accent));
        } else {
            tvVertical.setTextColor(getResources().getColor(R.color.app_color_accent));
        }

        if (Settings.isLockLandscape()) {
            tvLockLandscape.setTextColor(getResources().getColor(R.color.app_color_accent));
            if (!ScreenUtils.isLandscape()) ScreenUtils.setLandscape(this);
        } else {
            tvLockLandscape.setTextColor(Color.WHITE);
        }

        // 目录书签侧滑页初始化
        FragmentManager fm = getSupportFragmentManager();
        FragmentPagerAdapter adapter = new PagerAdapter(fm);
        vp.setAdapter(adapter);
        tabLayout.setupWithViewPager(vp);
        TabLayout.Tab tab1 = tabLayout.getTabAt(0);
        TabLayout.Tab tab2 = tabLayout.getTabAt(1);
        if (tab1 != null) tab1.setCustomView(R.layout.app_tab_content);
        if (tab2 != null) tab2.setCustomView(R.layout.app_tab_bookmark);

        getData(savedInstanceState);

        setListener();

        initPdf(uri, pdf);

        enterFullScreen();
    }

    private void getData(Bundle savedInstanceState) {
        Intent intent = getIntent();
        uri = intent.getData();
        pdf = intent.getParcelableExtra(EXTRA_PDF);
        if (pdf != null) {
            curPage = savedInstanceState != null ? savedInstanceState.getInt(BUNDLE_CUR_PAGE) : pdf.getCurPage();
            pageCount = pdf.getTotalPage();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListener() {
        ibtnQuickbarAction.setOnClickListener(v -> {
            // 当前页就是操作后的上一页或者下一页
            if (v.isSelected()) {
                previousPage = pdfView.getCurrentPage();
                pdfView.jumpTo(nextPage); // Redo
            } else {
                nextPage = pdfView.getCurrentPage();
                pdfView.jumpTo(previousPage); // Undo
            }
            v.setSelected(!v.isSelected());
        });
        tvPreviousChapter.setOnClickListener(v -> {
            // 减 1 是为了防止当前页面有标题的情况下无法跳转，因为是按标题来跳转
            int targetPage = pdfView.getCurrentPage() - 1;
            if (!pageList.isEmpty() && targetPage < pageList.size()) {
                if (llQuickBar.getVisibility() != View.VISIBLE) {
                    showQuickbar();
                }
                ibtnQuickbarAction.setSelected(false); // 将状态调为 Undo
                while (!pageList.contains((long) targetPage)) {
                    if (targetPage < pageList.get(0)) {
                        return; // 如果实在匹配不到就跳出方法，不执行跳转
                    }
                    targetPage--; // 如果匹配不到会一直减 1 搜索
                }
                previousPage = pdfView.getCurrentPage();
                pdfView.jumpTo(targetPage);
            }
        });
        tvNextChapter.setOnClickListener(v -> {
            // 这里的原理和上面跳转上一章节一样
            int targetPage = pdfView.getCurrentPage() + 1;
            if (!pageList.isEmpty() && targetPage < pageList.size()) {
                pdfViewBg.setVisibility(View.VISIBLE);
                if (llQuickBar.getVisibility() != View.VISIBLE) {
                    showQuickbar();
                }
                ibtnQuickbarAction.setSelected(false);
                while (!pageList.contains((long) targetPage)) {
                    if (targetPage > pageList.get(pageList.size() - 1)) {
                        return;
                    }
                    targetPage++;
                }
                previousPage = pdfView.getCurrentPage();
                pdfView.jumpTo(targetPage);
            }
        });
        tvContent.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                TabLayout.Tab tab = tabLayout.getTabAt(0);
                if (tab != null) tab.select();
                hideBar();
                enterFullScreen();
                openContent();
            }
        });
        tvReadMethod.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                hideBar();
                enterFullScreen();
                openReadMethod();
            }
        });
        tvAutoScroll.setOnClickListener(v -> {
            if (Settings.isSwipeHorizontal()) {
                UiManager.showCenterShort(R.string.app_horizontal_does_not_support_auto_scroll);
                return;
            }
            v.setSelected(!v.isSelected());
            if (v.isSelected()) {
                hideBar();
                enterFullScreen();
                autoDisp = Observable.interval(Settings.getScrollLevel(), TimeUnit.MILLISECONDS)
                        .doOnDispose(() -> {
                            tvAutoScroll.setSelected(false);
                            isPause = false;
                        })
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(PreviewActivity.this)))
                        .subscribe(aLong -> {
                            if (!pdfView.isRecycled() && !isPause) {
                                pdfView.moveRelativeTo(0, OFFSET_Y);
                                pdfView.loadPageByOffset();
                            }
                        });
            } else {
                if (!autoDisp.isDisposed()) autoDisp.dispose();
            }
        });
        tvBookmark.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            int curPage = pdfView.getCurrentPage();
            if (v.isSelected()) {
                drawBookmark(canvas, pageWidth);
                String title = getTitle(curPage);
                long time = System.currentTimeMillis();
                Bookmark bk = new Bookmark(curPage, title, time);
                bookmarkMap.put((long) curPage, bk);
            } else {
                bookmarkMap.remove((long) curPage);
            }
            bkFragInterface.update(bookmarkMap.values());
            pdfView.invalidate();
        });
        tvBookmark.setOnLongClickListener(view -> {
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            if (tab != null) tab.select();
            hideBar();
            enterFullScreen();
            openContent();
            return true;
        });
        tvMore.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                hideBar();
                enterFullScreen();
                openMore();
            }
        });
        tvLockLandscape.setOnClickListener(v -> {
            if (ScreenUtils.isPortrait()) {
                Settings.setLockLandscape(true);
                ScreenUtils.setLandscape(this);
            } else {
                Settings.setLockLandscape(false);
                ScreenUtils.setPortrait(this);
            }
        });
        tvSettings.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                hideBar();
                if (autoDisp != null && !autoDisp.isDisposed()) {
                    autoDisp.dispose();
                }
                SettingsActivity.start(PreviewActivity.this, REQUEST_CODE_SETTINGS);
            }
        });
        screenCover.setOnTouchListener((view, event) -> {
            if (vgContent.getTranslationX() == 0) {
                closeContent(null);
                return true;
            }
            return false;
        });
        tvHorizontal.setOnClickListener(view -> {
            closeReadMethod();
            if (!Settings.isSwipeHorizontal()) {
                if (autoDisp != null && !autoDisp.isDisposed()) {
                    autoDisp.dispose();
                    UiManager.showCenterShort(R.string.app_horizontal_does_not_support_auto_scroll);
                    return;
                }
                tvHorizontal.setTextColor(getResources().getColor(R.color.app_color_accent));
                tvVertical.setTextColor(getResources().getColor(R.color.base_white));
                Settings.setSwipeHorizontal(true);
                initPdf(uri, pdf);
            }
        });
        tvVertical.setOnClickListener(view -> {
            closeReadMethod();
            if (Settings.isSwipeHorizontal()) {
                tvVertical.setTextColor(getResources().getColor(R.color.app_color_accent));
                tvHorizontal.setTextColor(getResources().getColor(R.color.base_white));
                Settings.setSwipeHorizontal(false);
                initPdf(uri, pdf);
            }
        });
    }

    private void openReadMethod() {
        int screenHeight = ScreenUtils.getScreenHeight();
        int viewHeight = llReadMethod.getMeasuredHeight();
        if (llReadMethod.getTranslationY() < viewHeight) {
            viewHeight += ConvertUtils.dp2px(24);
        }
        llReadMethod.animate()
                .setDuration(200)
                .setStartDelay(100)
                .translationY(screenHeight - viewHeight)
                .start();
    }

    private void closeReadMethod() {
        int screenHeight = ScreenUtils.getScreenHeight();
        llReadMethod.animate()
                .setDuration(200)
                .translationY(screenHeight)
                .start();
    }

    private void openMore() {
        int screenHeight = ScreenUtils.getScreenHeight();
        int viewHeight = vgMore.getMeasuredHeight();
        if (vgMore.getTranslationY() < viewHeight) {
            viewHeight += ConvertUtils.dp2px(24);
        }
        vgMore.animate()
                .setDuration(200)
                .setStartDelay(100)
                .translationY(screenHeight - viewHeight)
                .start();
    }

    private void closeMore() {
        int screenHeight = ScreenUtils.getScreenHeight();
        vgMore.animate()
                .setDuration(200)
                .translationY(screenHeight)
                .start();
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initPdf(Uri uri, PDF pdf) {
        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvPageinfo.setText((i + 1) + " / " + pageCount);
                // Quickbar
                tvQuickbarTitle.setText(getTitle(i));
                tvQuickbarPageinfo.setText((i + 1) + " / " + pageCount);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ibtnQuickbarAction.setSelected(false);
                previousPage = seekBar.getProgress();

                showQuickbar();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                nextPage = seekBar.getProgress();
                pdfView.jumpTo(seekBar.getProgress());
            }
        });
        PDFView.Configurator configurator;
        if (uri != null) {
            File file = UriUtils.uri2File(uri);
            String path = file != null ? UriUtils.uri2File(uri).getAbsolutePath() : null;
            tvPageinfo.setText("1 / " + pageCount);
            String bookName = path != null ? path.substring(path.lastIndexOf("/") + 1, path.length() - 4) : "";
            mToolbar.post(() -> mToolbar.setTitle(bookName));
            configurator = pdfView.fromUri(uri).defaultPage(curPage);
        } else if (pdf != null) {
            String bkJson = pdf.getBookmark(); // 获取书签 json
            List<Bookmark> bkList = GsonUtils.fromJson(bkJson, new TypeToken<List<Bookmark>>(){}.getType());
            if (bkList != null) {
                for (Bookmark bk : bkList) {
                    bookmarkMap.put((long) bk.getPageId(), bk);
                }
            }
            sbProgress.setProgress(curPage);
            tvPageinfo.setText((curPage + 1) + " / " + pageCount);
            mToolbar.post(() -> mToolbar.setTitle(pdf.getName()));
            configurator = pdfView.fromFile(new File(pdf.getPath())).defaultPage(curPage);
        } else {
            UiManager.showCenterShort(R.string.app_file_is_empty);
            finish();
            return;
        }
        if (password != null) configurator = configurator.password(password);

        paint = new Paint();

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
                        if (!StringUtils.isEmpty(password)) {
                            UiManager.showCenterShort(R.string.app_password_error);
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
                    this.canvas = canvas;
                    this.pageWidth = pageWidth;
                    if (bookmarkMap.containsKey((long) displayedPage)) {
                        drawBookmark(canvas, pageWidth);
                    }
                })
                .onPageChange((page, pageCount) -> {
                    curPage = page;
                    tvBookmark.setSelected(bookmarkMap.containsKey((long) page)); // 如果是书签则标红

                    tvQuickbarTitle.setText(getTitle(page));
                    tvQuickbarPageinfo.setText((page + 1) + " / " + this.pageCount);

                    tvPageinfo.setText((page + 1) + " / " + this.pageCount);
                    sbProgress.setProgress(page);
                })
                .onLoad(nbPages -> {
                    pageCount = pdfView.getPageCount();
                    sbProgress.setMax(pageCount - 1);
                    List<PdfDocument.Bookmark> list = pdfView.getTableOfContents();
                    findContent(list);

                    List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
                    for (Fragment f : fragmentList) {
                        if (f instanceof IContentFragInterface) {
                            contentFragInterface = (IContentFragInterface) f;
                        } else if (f instanceof IBkFragInterface) {
                            bkFragInterface = (IBkFragInterface) f;
                        }
                    }
                    contentFragInterface.update(list);
                    bkFragInterface.update(bookmarkMap.values());

                    Set<Long> keySet = contentMap.keySet();
                    pageList.addAll(keySet);
                    Collections.sort(pageList);

                    if (!Settings.isNightMode()) {
                        pdfViewBg.setBackground(new ColorDrawable(Color.WHITE));
                        if (Settings.isSwipeHorizontal()) {
                            int page = Math.round(pdfView.getPageCount() / 2);
                            SizeF sizeF = pdfView.getPageSize(page);
                            ViewGroup.LayoutParams lp = pdfViewBg.getLayoutParams();
                            lp.width = (int) sizeF.getWidth();
                            lp.height = (int) sizeF.getHeight();
                            pdfViewBg.setLayoutParams(lp);
                        } else {
                            ViewGroup.LayoutParams lp = pdfViewBg.getLayoutParams();
                            lp.width = ScreenUtils.getScreenWidth();
                            lp.height = ScreenUtils.getScreenHeight();
                            pdfViewBg.setLayoutParams(lp);
                        }
                    } else {
                        pdfViewBg.setBackground(new ColorDrawable(Color.BLACK));
                    }
                })
                .onTap(event -> {
                    if (autoDisp != null && !autoDisp.isDisposed()) {
                        if (mToolbar.getAlpha() == 1.0F) {
                            hideBar();
                            enterFullScreen();
                        } else {
                            isPause = !isPause;
                        }
                        return true;
                    }
                    if (llReadMethod.getTranslationY() != ScreenUtils.getScreenHeight()) {
                        closeReadMethod();
                        return true;
                    }
                    if (vgMore.getTranslationY() != ScreenUtils.getScreenHeight()) {
                        closeMore();
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
                            int currentPage = pdfView.getCurrentPage();
                            pdfView.jumpTo(--currentPage, true);
                        }
                    } else if (ScreenUtils.isPortrait() && x >= next) {
                        if (mToolbar.getAlpha() == 1.0F) {
                            hideBar();
                            enterFullScreen();
                        } else {
                            int currentPage = pdfView.getCurrentPage();
                            pdfView.jumpTo(++currentPage, true);
                        }
                    } else {
                        boolean visible = mToolbar.getAlpha() == 1.0F
                                && llBottomBar.getAlpha() == 1.0F;
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
        btnCancel.setText(R.string.app_do_not_delete);
        btnConfirm.setText(R.string.app_confirm);
        btnCancel.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> {
            initPdf(uri, pdf);
            dialog.dismiss();
        });
        etInput.addTextChangedListener(new TextWatcherImpl() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                password = charSequence.toString();
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
        canvas.drawBitmap(bitmap, left, 0, paint);
    }

    private void showQuickbar() {
        llQuickBar.animate()
                .setDuration(50)
                .alpha(1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        llQuickBar.setVisibility(View.VISIBLE);
                    }
                })
                .start();
    }

    private void openContent() {
        vgContent.animate()
                .setDuration(250)
                .setStartDelay(100)
                .translationX(0)
                .setUpdateListener(valueAnimator -> {
                    screenCover.setAlpha(valueAnimator.getAnimatedFraction());
                })
                .start();
    }

    private void closeContent(Animator.AnimatorListener listener) {
        vgContent.animate()
                .setDuration(250)
                .translationX(-vgContent.getMeasuredWidth())
                .setUpdateListener(valueAnimator -> {
                    screenCover.setAlpha(1 - valueAnimator.getAnimatedFraction());
                })
                .setListener(listener)
                .start();
    }

    private void findContent(List<PdfDocument.Bookmark> list) {
        for (PdfDocument.Bookmark bk : list) {
            contentMap.put(bk.getPageIdx(), bk);
            if (bk.hasChildren()) {
                findContent(bk.getChildren());
            }
        }
    }

    private String getTitle(int page) {
        if (contentMap.isEmpty()) return getString(R.string.app_have_no_content);
        String title;
        PdfDocument.Bookmark bk = contentMap.get((long) page);
        title = bk != null ? bk.getTitle() : null;
        if (StringUtils.isEmpty(title)) {
            if (page < pageList.get(0)) {
                title = contentMap.get(pageList.get(0)).getTitle();
            } else {
                int index = pageList.indexOf((long) page);
                while (index == -1) {
                    index = pageList.indexOf((long) page--);
                }
                title = contentMap.get(pageList.get(index)).getTitle();
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
        llBottomBar.animate().setDuration(250).alpha(1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        llBottomBar.setVisibility(View.VISIBLE);
                    }
                }).start();
        tvPageinfo.animate().setDuration(250).alpha(1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        tvPageinfo.setVisibility(View.VISIBLE);
                    }
                }).start();
    }

    private void hideBar() {
        mToolbar.animate().setDuration(250).alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mToolbar != null) mToolbar.setVisibility(View.GONE);
                    }
                }).start();
        llBottomBar.animate().setDuration(250).alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (llBottomBar != null) llBottomBar.setVisibility(View.GONE);
                    }
                }).start();
        tvPageinfo.animate().setDuration(250).alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (tvPageinfo != null) tvPageinfo.setVisibility(View.GONE);
                    }
                }).start();
        llQuickBar.animate().setDuration(250).alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (llQuickBar != null) llQuickBar.setVisibility(View.GONE);
                    }
                }).start();
        if (ibtnQuickbarAction != null) ibtnQuickbarAction.setSelected(false); // 初始化为 Undo 状态
    }

    private void enterFullScreen() {
        if (Settings.isShowStatusBar()) {
            UiManager.setTranslucentStatusBar(this);
            vgContent.setPadding(0, ConvertUtils.dp2px(25), 0, 0);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            UiManager.setTransparentStatusBar(this);
            vgContent.setPadding(0, 0, 0, 0);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void exitFullScreen() {
        if (Settings.isShowStatusBar()) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//            mToolbar.post(() -> UiManager.setBlackNavigationBar(this));
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        mToolbar.post(() -> UiManager.setNavigationBarColor(this, getResources().getColor(R.color.base_black)));
    }
}
