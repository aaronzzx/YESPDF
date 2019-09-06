package com.aaron.yespdf.common;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import com.blankj.utilcode.util.PathUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class PdfUtils {

    public static Bitmap pdfToBitmap(String path, int curPage) throws IOException {
        PdfRenderer.Page page = new PdfRenderer(ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_WRITE)).openPage(curPage);
        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        return bitmap;
    }

    public static int getPdfTotalPage(String path) throws IOException {
        PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_WRITE));
        return renderer.getPageCount();
    }

    public static void saveBitmap(Bitmap bitmap, String savePath) {
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
