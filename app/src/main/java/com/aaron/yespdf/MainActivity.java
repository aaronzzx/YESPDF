package com.aaron.yespdf;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnTapListener;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity_main);
        final PDFView pdfView = findViewById(R.id.app_pdfView);
        View previous = findViewById(R.id.app_view_previous);
        View next = findViewById(R.id.app_view_next);
        previous.setOnClickListener(v -> pdfView.jumpTo(pdfView.getCurrentPage() - 1, true));
        next.setOnClickListener(v -> pdfView.jumpTo(pdfView.getCurrentPage() + 1, true));
        PermissionUtils.permission(PermissionConstants.STORAGE)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        String path = "/storage/emulated/0/Android#Java/Java/Java 编程思想(原书第4版).pdf";
                        pdfView.fromFile(new File(path))
                                .swipeHorizontal(true)
                                .pageSnap(true)
                                .load();
                    }

                    @Override
                    public void onDenied() {

                    }
                })
                .request();
    }
}
