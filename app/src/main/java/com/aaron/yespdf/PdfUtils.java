package com.aaron.yespdf;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
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

    private PdfUtils() {}
}
