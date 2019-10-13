package com.aaron.yespdf.filepicker;

import com.blankj.utilcode.util.SDCardUtils;
import com.blankj.utilcode.util.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class ViewAllPresenter extends IViewAllContract.P {

    private String curPath = ROOT_PATH;
    private boolean isFirstIn = true;

    ViewAllPresenter(IViewAllContract.V v) {
        super(v);
    }

    @Override
    IViewAllContract.M model() {
        return new ViewAllModel();
    }

    @Override
    void detachV() {
        model.saveLastPath(curPath);
        view = null;
        model = null;
    }

    @Override
    boolean canFinish() {
        return curPath.equals(ROOT_PATH);
    }

    @Override
    void goBack() {
        String prePath = curPath.substring(0, curPath.lastIndexOf("/"));
        listFile(prePath);
    }

    @Override
    void listStorage() {
        if (isFirstIn) {
            isFirstIn = false;
            String lastPath = model.queryLastPath();
            if (StringUtils.isEmpty(lastPath) || ROOT_PATH.equals(lastPath)) {
                listStorage();
            } else {
                listFile(lastPath);
            }
            return;
        }
        model.listStorage(new IViewAllContract.FileCallback<List<SDCardUtils.SDCardInfo>>() {
            @Override
            public void onResult(List<SDCardUtils.SDCardInfo> result) {
                fileList.clear();
                for (SDCardUtils.SDCardInfo info : result) {
                    if (info.getState().equals("mounted")) {
                        fileList.add(new File(info.getPath()));
                    }
                }
                view.onShowFileList(fileList);
            }
        });
    }

    @Override
    void listFile(final String path) {
        curPath = path;
        if (path.equals(ROOT_PATH)) {
            listStorage();
            return;
        }
        model.listFile(path, new IViewAllContract.FileCallback<List<File>>() {
            @Override
            public void onResult(List<File> result) {
                if (path.length() > 18) {
                    String path1 = path.substring(18); // 去除 /storage/emulated/
                    List<String> paths = Arrays.asList(path1.split("/"));
                    view.onShowPath(paths);
                }
                view.onShowFileList(result);
            }
        });
    }
}