package com.aaron.yespdf.main;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.filepicker.SelectActivity;
import com.blankj.utilcode.util.ThreadUtils;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class MainPresenter implements IMainContract.P {

    private IMainContract.V view;

    MainPresenter(IMainContract.V view) {
        this.view = view;
    }

    @Override
    public void insertPDF(Intent data) {
        view.onShowLoading();
        if (data != null) {
            List<String> pathList = data.getStringArrayListExtra(SelectActivity.EXTRA_SELECTED);
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
                view.onHideLoading();
                if (success) {
                    view.onShowMessage(R.string.app_import_success);
                    view.onUpdate();
                } else {
                    view.onShowMessage(R.string.app_import_failure);
                }
            }
        });
    }

    @Override
    public void detachV() {
        view = null;
    }
}
