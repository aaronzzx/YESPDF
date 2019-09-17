package com.aaron.yespdf.filepicker;

import androidx.annotation.StringRes;

import com.blankj.utilcode.util.SDCardUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
interface ISelectContract {

    interface M {
        void listStorage(FileCallback<List<SDCardUtils.SDCardInfo>> callback);

        void listFile(String path, FileCallback<List<File>> callback);
    }

    interface FileCallback<T> {
        void onResult(T result);
    }

    interface V {
        void attachP();

        void onShowMessage(@StringRes int stringId);

        void onShowFileList(List<File> fileList);

        void onShowPath(List<String> pathList);
    }

    abstract class P {
        static final String ROOT_PATH = "/storage/emulated";

        protected V mV;
        protected M mM;

        protected List<File> mFileList;

        P(V v) {
            mV = v;
            mM = model();
            mFileList = new ArrayList<>();
        }

        abstract M model();

        abstract void detachV();

        abstract boolean canFinish();

        abstract void goBack();

        abstract void listStorage();

        abstract void listFile(String path);
    }
}
