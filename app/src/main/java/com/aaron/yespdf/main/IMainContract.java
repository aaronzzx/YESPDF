package com.aaron.yespdf.main;

import android.content.Intent;

import androidx.annotation.StringRes;

import com.aaron.yespdf.common.IContract;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface IMainContract {

    interface V extends IContract.V {
        void onShowMessage(@StringRes int stringId);

        void onShowLoading();

        void onHideLoading();

        void onUpdate();
    }

    interface P extends IContract.P {
        void insertPDF(Intent data);
    }
}
