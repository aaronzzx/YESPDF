package com.aaron.yespdf.preview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.CommonActivity;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.PdfUtils;
import com.aaron.yespdf.common.UiManager;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.event.RecentPDFEvent;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.UriUtils;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
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

public class PreviewActivity extends CommonActivity {

    public static final String EXTRA_PDF = "EXTRA_PDF";
    private static final float PREVIOUS  = ScreenUtils.getScreenWidth() * 0.3F;
    private static final float NEXT      = ScreenUtils.getScreenWidth() * 0.7F;

    @BindView(R2.id.app_pdfview_bg) View mPDFViewBg;
    @BindView(R2.id.app_pdfview) PDFView mPDFView;
    @BindView(R2.id.app_ll_undoredobar) LinearLayout mLlQuickBar;
    @BindView(R2.id.app_quickbar_title) TextView mTvQuickbarTitle;
    @BindView(R2.id.app_tv_pageinfo2) TextView mTvQuickbarPageinfo;
    @BindView(R2.id.app_ibtn_quickbar_action) ImageButton mIbtnQuickbarAction;
    @BindView(R2.id.app_ll_bottombar) LinearLayout mLlBottomBar;
    @BindView(R2.id.app_tv_previous_chapter) TextView mTvPreviousChapter;
    @BindView(R2.id.app_tv_next_chapter) TextView mTvNextChapter;
    @BindView(R2.id.app_tv_pageinfo) TextView mTvPageinfo;
    @BindView(R.id.app_sb_progress) SeekBar mSbProgress;

    private PDF mPDF;
    private Map<Long, PdfDocument.Bookmark> mContentMap = new HashMap<>();
    private List<Long> mPageList = new ArrayList<>();

    private int mPreviousPage;
    private int mNextPage;

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
        initView(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        boolean visible = mToolbar.getAlpha() == 1.0F && mLlBottomBar.getAlpha() == 1.0F;
        if (visible) {
            enterFullScreen();
        }
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
            DBHelper.updatePDF(mPDF);
        }
        EventBus.getDefault().post(new RecentPDFEvent(true));
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
        return result+"%";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mToolbar.getAlpha() == 0.0F) {
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

    @SuppressLint({"SwitchIntDef"})
    private void initView(Bundle savedInstanceState) {
        UiManager.setTransparentStatusBar(this, mToolbar);
        UiManager.setNavigationBarColor(this, R.color.base_black);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.app_ic_action_back_white);
        }

        clickEvent();

        Intent intent = getIntent();
        Uri uri = intent.getData();
        PDF pdf = intent.getParcelableExtra(EXTRA_PDF);
        int pageCount;
        if (uri == null) {
            pageCount = pdf != null ? pdf.getTotalPage() : 0;
        } else {
            pageCount = PdfUtils.getPdfTotalPage(UriUtils.uri2File(uri).getAbsolutePath());
        }

        loadPdf(uri, pdf, pageCount);
        enterFullScreen();
    }

    private void clickEvent() {
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
            if (mLlQuickBar.getAlpha() != 1.0F) {
                mLlQuickBar.animate().setDuration(50).alpha(1).start();
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
            if (mLlQuickBar.getAlpha() != 1.0F) {
                mLlQuickBar.animate().setDuration(50).alpha(1).start();
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
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void loadPdf(Uri uri, PDF pdf, int pageCount) {
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
                mIbtnQuickbarAction.setSelected(false);
                mPreviousPage = seekBar.getProgress();

                mLlQuickBar.animate().setDuration(50).alpha(1).start();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mNextPage = seekBar.getProgress();

                mPDFView.jumpTo(seekBar.getProgress());
            }
        });
        PDFView.Configurator configurator;
        if (uri != null) {
            String path = UriUtils.uri2File(uri).getAbsolutePath();
            mTvPageinfo.setText("1 / " + pageCount);
            String bookName = path.substring(path.lastIndexOf("/") + 1, path.length() - 4);
            mToolbar.post(() -> mToolbar.setTitle(bookName));
            configurator = mPDFView.fromUri(uri);
        } else if (pdf != null) {
            mPDF = pdf;
            int curPage = pdf.getCurPage(); // 因为从0计数
            mSbProgress.setProgress(curPage);
            mTvPageinfo.setText((curPage + 1) + " / " + pageCount);
            mToolbar.post(() -> mToolbar.setTitle(pdf.getName()));
            configurator = mPDFView.fromFile(new File(pdf.getPath())).defaultPage(curPage);
        } else {
            return;
        }

        configurator.enableDoubletap(false)
                .disableLongpress()
                .enableDoubletap(false)
                .pageFling(true)
                .swipeHorizontal(true)
                .pageSnap(true)
                .fitEachPage(true)
                .autoSpacing(true)
                .onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {

                    }
                })
                .onPageError(new OnPageErrorListener() {
                    @Override
                    public void onPageError(int page, Throwable t) {

                    }
                })
                .onPageChange((page, pageCount1) -> {
                    mTvQuickbarTitle.setText(getTitle(page));
                    mTvQuickbarPageinfo.setText((page + 1) + " / " + pageCount);

                    mTvPageinfo.setText((page + 1) + " / " + pageCount);
                    mSbProgress.setProgress(page);
                })
                .onLoad(nbPages -> {
                    findContent(mPDFView.getTableOfContents());
                    Set<Long> keySet = mContentMap.keySet();
                    mPageList.addAll(keySet);
                    Collections.sort(mPageList);

                    int page = Math.round(mPDFView.getPageCount() / 2);
                    SizeF sizeF = mPDFView.getPageSize(page);
                    ViewGroup.LayoutParams lp = mPDFViewBg.getLayoutParams();
                    lp.width = (int) sizeF.getWidth();
                    lp.height = (int) sizeF.getHeight();
                    mPDFViewBg.setLayoutParams(lp);
                })
                .onTap(event -> {
                    float x = event.getRawX();
                    if (x <= PREVIOUS) {
                        if (mToolbar.getAlpha() == 1.0F) {
                            enterFullScreen();
                            return true;
                        }
                        int currentPage = mPDFView.getCurrentPage();
                        mPDFView.jumpTo(--currentPage, true);
                    } else if (x >= NEXT) {
                        if (mToolbar.getAlpha() == 1.0F) {
                            enterFullScreen();
                            return true;
                        }
                        int currentPage = mPDFView.getCurrentPage();
                        mPDFView.jumpTo(++currentPage, true);
                    } else {
                        boolean visible = mToolbar.getAlpha() == 1.0F && mLlBottomBar.getAlpha() == 1.0F;
                        if (visible) {
                            enterFullScreen();
                        } else {
                            exitFullScreen();
                        }
                    }
                    return true;
                })
                .load();
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

    private void enterFullScreen() {
        mToolbar.animate().setDuration(250).alpha(0).start();
        mLlBottomBar.animate().setDuration(250).alpha(0).start();
        mTvPageinfo.animate().setDuration(250).alpha(0).start();
        mLlQuickBar.animate().setDuration(250).alpha(0).start();
        mIbtnQuickbarAction.setSelected(false); // 初始化为 Undo 状态

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void exitFullScreen() {
        mToolbar.animate().setDuration(250).alpha(1).start();
        mLlBottomBar.animate().setDuration(250).alpha(1).start();
        mTvPageinfo.animate().setDuration(250).alpha(1).start();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
}
