package com.aaron.yespdf.main;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.DataManager;
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
            int type = data.getIntExtra(SelectActivity.EXTRA_TYPE, 0);
            String groupName = data.getStringExtra(SelectActivity.EXTRA_GROUP_NAME);
            insertPDF(pathList, type, groupName);
        }
    }

    private void insertPDF(List<String> pathList, int type, String groupName) {
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
            @NonNull
            @Override
            public Boolean doInBackground() {
                switch (type) {
                    case SelectActivity.TYPE_BASE_FOLDER:
                        DBHelper.insert(pathList);
                        break;
                    case SelectActivity.TYPE_TO_EXIST:
                    case SelectActivity.TYPE_CUSTOM:
                        DBHelper.insert(pathList, groupName);
                        break;
                }
                return false;
            }

            @Override
            public void onSuccess(Boolean success) {
                DataManager.updateAll();
                view.onUpdate();
                view.onHideLoading();
//                if (success) {
//                    view.onShowMessage(R.string.app_import_success);
//                    view.onUpdate();
//                } else {
//                    view.onShowMessage(R.string.app_import_failure);
//                }
            }
        });
    }

    @Override
    public void detachV() {
        view = null;
    }
}
