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
import com.aaron.yespdf.common.PdfUtils;
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
public class AllFragment extends BaseFragment implements AllFragmentComm {

    @BindView(R2.id.app_rv_all) RecyclerView mRvAll;

    private Unbinder mUnbinder;
    private RecyclerView.Adapter mAdapter;

    private List<Collection> mCollections = new ArrayList<>();

    static Fragment newInstance() {
        return new AllFragment();
    }

    public AllFragment() {

    }

    @Override
    public void update(List<String> pathList) {
        mCollections.clear();
        mCollections.addAll(DBHelper.queryAllCollection());
        mAdapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.app_fragment_all, container, false);
        mUnbinder = ButterKnife.bind(this, layout);
        initView();
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void initView() {
        mCollections.addAll(DBHelper.queryAllCollection());

        mRvAll.addItemDecoration(new XGridDecoration());
        mRvAll.addItemDecoration(new YGridDecoration());
        RecyclerView.LayoutManager lm = new GridLayoutManager(mActivity, 3);
        mRvAll.setLayoutManager(lm);
        mAdapter = new AllAdapter(mCollections);
        mRvAll.setAdapter(mAdapter);
    }
}
