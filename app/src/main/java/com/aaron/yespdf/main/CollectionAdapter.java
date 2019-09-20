package com.aaron.yespdf.main;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.bean.PDF;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class CollectionAdapter extends AbstractPDFAdapter {

    private IFragmentInterface activityInterface;

    CollectionAdapter(IFragmentInterface activityInterface, List<PDF> pdfList) {
        super(pdfList);
        this.activityInterface = activityInterface;
    }

    @Override
    int itemView() {
        return R.layout.app_recycler_item_cover;
    }

    @Override
    void startOperation() {
        activityInterface.startOperation();
        EventBus.getDefault().post(new OperationEvent());
    }

    @Override
    void onSelect(List<PDF> list, boolean isSelectAll) {
        activityInterface.onSelect(list, isSelectAll);
    }
}
