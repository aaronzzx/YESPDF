package com.aaron.yespdf.filepicker;

import com.blankj.utilcode.util.SDCardUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.io.File;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class ViewAllModel implements IViewAllContract.M {

    private IListable mListable;

    ViewAllModel() {
        mListable = new ByNameListable();
    }

    @Override
    public void listStorage(IViewAllContract.FileCallback<List<SDCardUtils.SDCardInfo>> callback) {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<List<SDCardUtils.SDCardInfo>>() {
            @Override
            public List<SDCardUtils.SDCardInfo> doInBackground() {
                return SDCardUtils.getSDCardInfo();
            }

            @Override
            public void onSuccess(List<SDCardUtils.SDCardInfo> result) {
                callback.onResult(result);
            }
        });
    }

    @Override
    public void listFile(String path, IViewAllContract.FileCallback<List<File>> callback) {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<List<File>>() {
            @Override
            public List<File> doInBackground() {
                return mListable.listFile(path);
            }

            @Override
            public void onSuccess(List<File> result) {
                callback.onResult(result);
            }
        });
    }

    @Override
    public void saveLastPath(String path) {
        SPStaticUtils.put(SP_LAST_PATH, path);
    }

    @Override
    public String queryLastPath() {
        return SPStaticUtils.getString(SP_LAST_PATH, "");
    }
}
