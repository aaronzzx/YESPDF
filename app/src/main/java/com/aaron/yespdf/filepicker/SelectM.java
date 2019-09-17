package com.aaron.yespdf.filepicker;

import com.blankj.utilcode.util.SDCardUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.io.File;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class SelectM implements ISelectContract.M {

    private IListable mListable;

    SelectM() {
        mListable = new ByNameListable();
    }

    @Override
    public void listStorage(ISelectContract.FileCallback<List<SDCardUtils.SDCardInfo>> callback) {
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
    public void listFile(String path, ISelectContract.FileCallback<List<File>> callback) {
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
}
