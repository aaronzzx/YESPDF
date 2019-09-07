package com.aaron.yespdf.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

import com.aaron.base.impl.OnClickListenerImpl;
import com.aaron.base.util.TimerUtils;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.CommonActivity;
import com.aaron.yespdf.common.PdfUtils;
import com.aaron.yespdf.common.UiManager;
import com.aaron.yespdf.common.bean.PDF;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.UriUtils;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.shockwave.pdfium.PdfDocument;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewActivity extends CommonActivity {

    public static final String EXTRA_PDF = "EXTRA_PDF";

    @BindView(R2.id.app_pdfview_bg)
    View mPDFViewBg;
    @BindView(R2.id.app_pdfview)
    PDFView mPDFView;
    @BindView(R2.id.app_action_previous)
    View mActionPrevious;
    @BindView(R2.id.app_action_next)
    View mActionNext;
    @BindView(R2.id.app_ll_bottombar)
    LinearLayout mLlBottomBar;
    @BindView(R2.id.app_tv_pageinfo)
    TextView mTvPageinfo;
    @BindView(R.id.app_sb_progress)
    SeekBar mSbProgress;
    @BindView(R.id.app_ibtn_content)
    ImageButton mIbtnContent;

    private List<PdfDocument.Bookmark> mContentList = new ArrayList<>();

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
        boolean visible = mToolbar.getVisibility() == View.VISIBLE && mLlBottomBar.getVisibility() == View.VISIBLE;
        if (visible) {
            enterFullScreen();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPDFView.recycle();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mToolbar.getVisibility() == View.GONE) {
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
        UiManager.setTransparentNavigationBar(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.app_ic_action_back_white);
        }
        mActionPrevious.setOnClickListener(v -> {
            if (mToolbar.getVisibility() == View.VISIBLE) {
                enterFullScreen();
                return;
            }
            int currentPage = mPDFView.getCurrentPage();
            mPDFView.jumpTo(--currentPage, true);
        });
        mActionNext.setOnClickListener(v -> {
            if (mToolbar.getVisibility() == View.VISIBLE) {
                enterFullScreen();
                return;
            }
            int currentPage = mPDFView.getCurrentPage();
            mPDFView.jumpTo(++currentPage, true);
        });
        mIbtnContent.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                // TODO: 2019/9/7 跳转目录
            }
        });

        Intent intent = getIntent();
        Uri uri = intent.getData();
        PDF pdf = intent.getParcelableExtra(EXTRA_PDF);

        loadPdf(uri, pdf);
        enterFullScreen();
    }

    private void loadPdf(Uri uri, PDF pdf) {
        PDFView.Configurator configurator;
        if (uri != null) {
            String path = UriUtils.uri2File(uri).getAbsolutePath();
            int pageCount = PdfUtils.getPdfTotalPage(path);
            mSbProgress.setMax(pageCount);
            mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    mTvPageinfo.setText(i + " / " + pageCount);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mPDFViewBg.setVisibility(View.VISIBLE);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int cur = seekBar.getProgress();
                    mPDFView.jumpTo(cur);
                }
            });
            mTvPageinfo.setText("0 / " + pageCount);
            String bookName = path.substring(path.lastIndexOf("/") + 1, path.length() - 4);
            mToolbar.post(() -> mToolbar.setTitle(bookName));
            configurator = mPDFView.fromUri(uri);
        } else if (pdf != null) {
            mSbProgress.setMax(pdf.getTotalPage());
            mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    mTvPageinfo.setText(i + " / " + pdf.getTotalPage());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mPDFViewBg.setVisibility(View.VISIBLE);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int cur = seekBar.getProgress();
                    mPDFView.jumpTo(cur);
                }
            });
            mTvPageinfo.setText("0 / " + pdf.getTotalPage());
            mToolbar.post(() -> mToolbar.setTitle(pdf.getName()));
            configurator = mPDFView.fromFile(new File(pdf.getPath())).defaultPage(pdf.getCurPage());
        } else {
            return;
        }
        configurator.enableDoubletap(false)
                .pageFling(true)
                .swipeHorizontal(true)
                .pageSnap(true)
                .fitEachPage(true)
                .spacing(ConvertUtils.dp2px(4))
                .onPageChange(((page, pageCount) -> {
                    TimerUtils.start(500, PreviewActivity.this, () -> mPDFViewBg.setVisibility(View.GONE));
                }))
                .onDraw((canvas, pageWidth, pageHeight, displayedPage) -> {
                    ViewGroup.LayoutParams lp = mPDFViewBg.getLayoutParams();
                    lp.width = (int) pageWidth;
                    lp.height = (int) pageHeight;
                    mPDFViewBg.setLayoutParams(lp);
                    mPDFViewBg.invalidate();
                })
                .onLoad(nbPages -> {
                    mContentList = mPDFView.getTableOfContents();
                })
                .onTap(event -> {
                    boolean visible = mToolbar.getVisibility() == View.VISIBLE && mLlBottomBar.getVisibility() == View.VISIBLE;
                    if (visible) {
                        enterFullScreen();
                    } else {
                        exitFullScreen();
                    }
                    return true;
                })
                .load();
    }

    private void enterFullScreen() {
        mToolbar.animate().setDuration(200).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mToolbar.setVisibility(View.GONE);
            }
        }).start();
        mLlBottomBar.animate().setDuration(200).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLlBottomBar.setVisibility(View.GONE);
            }
        }).start();
        mTvPageinfo.animate().setDuration(200).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mTvPageinfo.setVisibility(View.GONE);
            }
        }).start();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void exitFullScreen() {
        mToolbar.animate().setDuration(200).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mToolbar.setVisibility(View.VISIBLE);
            }
        }).start();
        mLlBottomBar.animate().setDuration(200).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLlBottomBar.setVisibility(View.VISIBLE);
            }
        }).start();
        mTvPageinfo.animate().setDuration(200).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mTvPageinfo.setVisibility(View.VISIBLE);
            }
        }).start();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
}
