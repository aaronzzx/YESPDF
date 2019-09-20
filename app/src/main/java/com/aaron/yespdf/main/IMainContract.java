package com.aaron.yespdf.main;

import android.content.Intent;

import androidx.annotation.StringRes;

import com.aaron.yespdf.common.IContract;
import com.aaron.yespdf.common.bean.Collection;
import com.aaron.yespdf.common.bean.PDF;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface IMainContract {

    interface V extends IContract.V {
        void onShowOperationBar();

        void onHideOperationBar();

        void onShowMessage(@StringRes int stringId);

        void onShowLoading();

        void onHideLoading();

        void onUpdate(int type);
    }

    interface P extends IContract.P {
        void insertPDF(Intent data);

        void deleteRecent(List<PDF> list);

        void deleteCollection(List<Collection> list);

        void showOperationBar();

        void hideOperationBar();
    }
}
