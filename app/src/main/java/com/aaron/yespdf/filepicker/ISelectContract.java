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
        String SP_LAST_PATH = "SP_LAST_PATH"; // 使用首选项存放退出之前的路径

        void listStorage(FileCallback<List<SDCardUtils.SDCardInfo>> callback);

        void listFile(String path, FileCallback<List<File>> callback);

        void saveLastPath(String path);

        String queryLastPath();
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

        protected V view;
        protected M model;

        protected List<File> fileList;

        P(V view) {
            this.view = view;
            model = model();
            fileList = new ArrayList<>();
        }

        abstract M model();

        abstract void detachV();

        abstract boolean canFinish();

        abstract void goBack();

        abstract void listStorage();

        abstract void listFile(String path);
    }
}
