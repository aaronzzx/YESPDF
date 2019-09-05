package com.aaron.yespdf.preview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.aaron.yespdf.CommonActivity;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewActivity extends CommonActivity {

    public static final String EXTRA_PATH = "EXTRA_PATH";

    @BindView(R2.id.app_pdfview)
    PDFView mPDFView;

    public static void start(Context context, String path) {
        Intent starter = new Intent(context, PreviewActivity.class);
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
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void initView(Bundle savedInstanceState) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.app_ic_action_back_black);
        }
        mToolbar.setVisibility(View.GONE);

        Intent intent = getIntent();
        String path = intent.getStringExtra(EXTRA_PATH);
        String bookName = path.substring(path.lastIndexOf("/") + 1, path.length() - 4);
        mToolbar.setTitle(bookName);
        mPDFView.fromFile(new File(path != null ? path : ""))
//                .swipeHorizontal(true)
                .pageFling(false)
                .load();
    }
}
