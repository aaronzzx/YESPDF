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
import com.aaron.yespdf.common.bean.PDF;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class RecentFragment extends BaseFragment {

    @BindView(R2.id.app_rv_recent) RecyclerView mRvRecent;

    private Unbinder mUnbinder;

    private List<PDF> mRecentPDFList = new ArrayList<>();

    static Fragment newInstance() {
        return new RecentFragment();
    }

    public RecentFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.app_fragment_recent, container, false);
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
        initData();

        mRvRecent.addItemDecoration(new XGridDecoration());
        mRvRecent.addItemDecoration(new YGridDecoration());
        RecyclerView.LayoutManager lm = new GridLayoutManager(mActivity, 3);
        mRvRecent.setLayoutManager(lm);
        RecyclerView.Adapter adapter = new PDFAdapter(mRecentPDFList);
        mRvRecent.setAdapter(adapter);
    }

    private void initData() {
        mRecentPDFList.addAll(DBHelper.queryRecentPDF());
    }
}
