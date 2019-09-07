package com.aaron.yespdf.common;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import com.blankj.utilcode.util.PathUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class PdfUtils {

    public static Bitmap pdfToBitmap(String path, int curPage) throws IOException {
        if (path == null) return null;
        PdfRenderer.Page page = new PdfRenderer(ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_WRITE)).openPage(curPage);
        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        return bitmap;
    }

    public static int getPdfTotalPage(String path) {
        PdfRenderer renderer = null;
        try {
            renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_WRITE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (renderer != null) {
            return renderer.getPageCount();
        }
        return 0;
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
//            renderer.close();
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
//            renderer.close();
        }
        return height;
    }

    public static void saveBitmap(Bitmap bitmap, String savePath) {
        if (bitmap == null) return;
        File file = new File(savePath);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PdfUtils() {}
}
