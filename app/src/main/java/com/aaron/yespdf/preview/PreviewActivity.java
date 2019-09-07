package com.aaron.yespdf.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.aaron.yespdf.common.CommonActivity;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.PdfUtils;
import com.aaron.yespdf.common.UiManager;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.shockwave.pdfium.PdfDocument;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewActivity extends CommonActivity {

    public static final String EXTRA_PATH = "EXTRA_PATH";

    @BindView(R2.id.app_pdfview) PDFView mPDFView;
    @BindView(R2.id.app_action_previous) View mActionPrevious;
    @BindView(R2.id.app_action_next) View mActionNext;

    public static void start(Context context, String path) {
        Intent starter = new Intent(context, PreviewActivity.class);
        starter.setDataAndType(Uri.fromFile(new File(path)), "application/pdf");
        starter.putExtra(EXTRA_PATH, path);
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
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
        UiManager.setBlackNavigationBar(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.app_ic_action_back_white);
        }
        mToolbar.setVisibility(View.GONE);
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

        Intent intent = getIntent();
        Uri uri = intent.getData();
        String path = uri != null ? uri.getPath() : null;
        String bookName = path != null ? path.substring(path.lastIndexOf("/") + 1, path.length() - 4) : null;
        mToolbar.post(() -> mToolbar.setTitle(bookName));
        mPDFView.fromUri(uri)
                .enableDoubletap(false)
                .pageFling(false)
                .swipeHorizontal(true)
                .pageSnap(true)
                .fitEachPage(true)
                .spacing(ConvertUtils.dp2px(4))
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        List<PdfDocument.Bookmark> bkList = mPDFView.getTableOfContents();
                        LogUtils.e(bkList.size());
                        for (PdfDocument.Bookmark bk : bkList) {
                            LogUtils.e(bk.getTitle() + "-" + bk.getPageIdx());
                            if (bk.hasChildren()) {
                                List<PdfDocument.Bookmark> list = bk.getChildren();
                                for (PdfDocument.Bookmark b : list) {
                                    LogUtils.e(b.getTitle() + "-" + b.getPageIdx());
                                }
                            }
                        }
                    }
                })
                .onTap(event -> {
                    switch (mToolbar.getVisibility()) {
                        case View.VISIBLE:
                            enterFullScreen();
                            break;
                        case View.GONE:
                            exitFullScreen();
                            break;
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

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
}
