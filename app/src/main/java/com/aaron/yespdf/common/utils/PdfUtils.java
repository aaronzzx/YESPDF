package com.aaron.yespdf.common.utils;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class PdfUtils {

    public static Bitmap pdfToBitmap(String path, int curPage) {
        if (StringUtils.isEmpty(path)) return null;
        PdfRenderer.Page page = null;
        try {
            page = new PdfRenderer(ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_WRITE)).openPage(curPage);
        } catch (SecurityException e) {
            e.printStackTrace();
        } finally {
            if (page != null) {
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                return bitmap;
            }
            return null;
        }
    }

    public static String saveBitmap(Bitmap bitmap, String savePath) {
        LogUtils.e("bitmap: " + bitmap);
        LogUtils.e("savePath: " + savePath);
        if (bitmap == null) return null;
        File file = new File(savePath);
        if (file.exists()) return savePath;
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return savePath;
    }

    public static int getPdfTotalPage(String path) {
        PdfRenderer renderer = null;
        try {
            renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_WRITE));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (renderer != null) {
                return renderer.getPageCount();
            }
            return 0;
        }
    }

    public static int getPdfWidth(String path, int page) {
        PdfRenderer renderer = null;
        try {
            renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_WRITE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int width = 0;
        if (renderer != null) {
            width = renderer.openPage(page).getWidth();
        }
        return width;
    }

    public static int getPdfHeight(String path, int page) {
        PdfRenderer renderer = null;
        try {
            renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_WRITE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int height = 0;
        if (renderer != null) {
            height = renderer.openPage(page).getHeight();
        }
        return height;
    }

    private PdfUtils() {}
}
