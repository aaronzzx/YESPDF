package com.aaron.yespdf.main;

import android.app.Activity;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.bean.PDF;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class AllPDFAdapter extends AbstractPDFAdapter {

    AllPDFAdapter(Activity activity, List<PDF> pdfList) {
        super(pdfList);
        context = activity;
    }

    @Override
    int itemView() {
        return R.layout.app_recycler_item_pdf;
    }

    @Override
    void startOperation() {
        EventBus.getDefault().post(new OperationEvent());
    }

    @Override
    void onSelect(List<PDF> list, boolean isSelectAll) {
        EventBus.getDefault().post(new SelectEvent(list, isSelectAll));
    }
}
