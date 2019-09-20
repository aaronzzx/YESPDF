package com.aaron.yespdf.main;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.bean.Collection;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.filepicker.DeprecateSelectActivity;
import com.blankj.utilcode.util.ThreadUtils;

import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class MainPresenter implements IMainContract.P {

    private IMainContract.V view;

    MainPresenter(IMainContract.V v) {
        view = v;
    }

    @Override
    public void insertPDF(Intent data) {
        view.onShowLoading();
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
                view.onHideLoading();
                if (success) {
                    view.onShowMessage(R.string.app_import_success);
                    view.onUpdate(1);
                } else {
                    view.onShowMessage(R.string.app_import_failure);
                }
            }
        });
    }

    @Override
    public void deleteRecent(List<PDF> list) {
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Object>() {
            @Override
            public Object doInBackground() {
                DBHelper.deleteRecent(list);
                return null;
            }

            @Override
            public void onSuccess(Object success) {
                view.onHideLoading();
                view.onUpdate(0);
                view.onHideOperationBar();
            }
        });
    }

    @Override
    public void deleteCollection(List<Collection> list) {
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Object>() {
            @Override
            public Object doInBackground() {
                DBHelper.deleteCollection(list);
                return null;
            }

            @Override
            public void onSuccess(Object success) {
                view.onHideLoading();
                view.onUpdate(1);
                view.onHideOperationBar();
            }
        });
    }

    @Override
    public void showOperationBar() {
        view.onShowOperationBar();
    }

    @Override
    public void hideOperationBar() {
        view.onHideOperationBar();
    }

    @Override
    public void detachV() {
        view = null;
    }
}
