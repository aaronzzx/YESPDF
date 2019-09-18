package com.aaron.yespdf.main;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.filepicker.DeprecateSelectActivity;
import com.blankj.utilcode.util.ThreadUtils;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class MainP implements IMainContract.P {

    private IMainContract.V mV;

    MainP(IMainContract.V v) {
        mV = v;
    }

    @Override
    public void insertPDF(Intent data) {
        mV.onShowLoading();
        if (data != null) {
            List<String> pathList = data.getStringArrayListExtra(DeprecateSelectActivity.EXTRA_SELECTED);
            insertPDF(pathList);
        }
    }

    private void insertPDF(List<String> pathList) {
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
            @NonNull
            @Override
            public Boolean doInBackground() {
                return DBHelper.insert(pathList);
            }

            @Override
            public void onSuccess(Boolean success) {
                mV.onHideLoading();
                if (success) {
                    mV.onShowMessage(R.string.app_import_success);
                    mV.onUpdate();
                } else {
                    mV.onShowMessage(R.string.app_import_failure);
                }
            }
        });
    }

    @Override
    public void detachV() {
        mV = null;
    }
}
