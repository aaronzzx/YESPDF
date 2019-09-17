package com.aaron.yespdf.filepicker;

import com.blankj.utilcode.util.SDCardUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class SelectP extends ISelectContract.P {

    private String mCurPath = ROOT_PATH;

    SelectP(ISelectContract.V v) {
        super(v);
    }

    @Override
    ISelectContract.M model() {
        return new SelectM();
    }

    @Override
    void detachV() {
        mV = null;
        mM = null;
    }

    @Override
    boolean canFinish() {
        return mCurPath.equals(ROOT_PATH);
    }

    @Override
    void goBack() {
        String prePath = mCurPath.substring(0, mCurPath.lastIndexOf("/"));
        listFile(prePath);
    }

    @Override
    void listStorage() {
        mM.listStorage(new ISelectContract.FileCallback<List<SDCardUtils.SDCardInfo>>() {
            @Override
            public void onResult(List<SDCardUtils.SDCardInfo> result) {
                mFileList.clear();
                for (SDCardUtils.SDCardInfo info : result) {
                    if (info.getState().equals("mounted")) {
                        mFileList.add(new File(info.getPath()));
                    }
                }
                mV.onShowFileList(mFileList);
            }
        });
    }

    @Override
    void listFile(final String path) {
        mCurPath = path;
        if (path.equals(ROOT_PATH)) {
            listStorage();
            return;
        }
        mM.listFile(path, new ISelectContract.FileCallback<List<File>>() {
            @Override
            public void onResult(List<File> result) {
                if (path.length() > 18) {
                    String path1 = path.substring(18); // 去除 /storage/emulated/
                    List<String> paths = Arrays.asList(path1.split("/"));
                    mV.onShowPath(paths);
                }
                mV.onShowFileList(result);
            }
        });
    }
}
