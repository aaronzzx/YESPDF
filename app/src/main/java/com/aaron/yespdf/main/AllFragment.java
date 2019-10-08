package com.aaron.yespdf.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.base.BaseFragment;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.UiManager;
import com.aaron.yespdf.common.bean.Collection;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class AllFragment extends BaseFragment implements IOperation, AbstractAdapter.ICommInterface<Collection> {

    @BindView(R2.id.app_rv_all)
    RecyclerView rvAll;

    private Unbinder unbinder;
    private AbstractAdapter<Collection> adapter;

    private List<Collection> collections = new ArrayList<>();
    private List<Collection> selectCollections = new ArrayList<>();

    static Fragment newInstance() {
        return new AllFragment();
    }

    public AllFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        View layout = inflater.inflate(R.layout.app_fragment_all, container, false);
        unbinder = ButterKnife.bind(this, layout);
        initView();
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) mActivity).setOperation(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPdfDeleteEvent(PdfDeleteEvent event) {
        LogUtils.e(event);
        if (event.isEmpty) {
            DBHelper.deleteCollection(event.dir);
        }
        update();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDirNameEvent(DirNameEvent event) {
        update();
    }

    @Override
    public void onStartOperation() {
        ((MainActivity) mActivity).startOperation();
    }

    @Override
    public void onSelect(List<Collection> list, boolean selectAll) {
        LogUtils.e(list);
        selectCollections.clear();
        selectCollections.addAll(list);
        ((MainActivity) mActivity).selectResult(list.size(), selectAll);
    }

    @Override
    public void delete() {
        if (!selectCollections.isEmpty()) {
            ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<List<String>>() {
                @Override
                public List<String> doInBackground() {
                    collections.removeAll(selectCollections);
                    return DBHelper.deleteCollection(selectCollections);
                }

                @Override
                public void onSuccess(List<String> dirList) {
                    UiManager.showShort(R.string.app_delete_completed);
                    ((MainActivity) mActivity).finishOperation();
                    adapter.notifyDataSetChanged();
                    EventBus.getDefault().post(new AllDeleteEvent(dirList));
                }
            });
        }
    }

    @Override
    public void selectAll(boolean selectAll) {
        adapter.selectAll(selectAll);
    }

    @Override
    public void cancelSelect() {
        adapter.cancelSelect();
    }

    @Override
    public String deleteDescription() {
        return getString(R.string.app_will_delete) + " " + selectCollections.size() + " " + getString(R.string.app_delete_for_all);
    }

    void update() {
        collections.clear();
        collections.addAll(DBHelper.queryAllCollection());
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        collections.addAll(DBHelper.queryAllCollection());

        rvAll.addItemDecoration(new XGridDecoration());
        rvAll.addItemDecoration(new YGridDecoration());
        GridLayoutManager lm = new GridLayoutManager(mActivity, 3);
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (collections.isEmpty()) {
                    return 3;
                }
                return 1;
            }
        });
        rvAll.setLayoutManager(lm);
        adapter = new AllAdapter(this, getFragmentManager(), collections);
        rvAll.setAdapter(adapter);
    }
}
