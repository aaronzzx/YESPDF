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
import com.aaron.yespdf.common.bean.Collection;
import com.blankj.utilcode.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class AllFragmentTest extends BaseFragment implements IOperation, AbstractAdapter.ICommInterface<Collection> {

    @BindView(R2.id.app_rv_all)
    RecyclerView rvAll;

    private Unbinder unbinder;
    private AbstractAdapter<Collection> adapter;

    private List<Collection> collections = new ArrayList<>();

    static Fragment newInstance() {
        return new AllFragmentTest();
    }

    public AllFragmentTest() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.app_fragment_all, container, false);
        unbinder = ButterKnife.bind(this, layout);
        initView();
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivityTest) mActivity).setOperation(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStartOperation() {
        ((MainActivityTest) mActivity).showOperationBar();
    }

    @Override
    public void onSelect(List<Collection> list, boolean selectAll) {
        LogUtils.e(list);
        ((MainActivityTest) mActivity).selectResult(list.size(), selectAll);
    }

    @Override
    public void delete() {

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
    public void update() {
        collections.clear();
        collections.addAll(DBHelper.queryAllCollection());
        adapter.notifyItemRangeChanged(0, collections.size(), 0);
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
        adapter = new AllAdapterTest(this, getFragmentManager(), collections);
        rvAll.setAdapter(adapter);
    }
}
