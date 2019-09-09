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
import com.aaron.yespdf.common.event.RecentPDFEvent;

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
public class RecentFragment extends BaseFragment {

    @BindView(R2.id.app_rv_recent) RecyclerView mRvRecent;

    private Unbinder mUnbinder;
    private RecyclerView.Adapter mAdapter;

    private List<PDF> mRecentPDFList = new ArrayList<>();

    static Fragment newInstance() {
        return new RecentFragment();
    }

    public RecentFragment() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecentPDFEvent(RecentPDFEvent event) {
        mRecentPDFList.clear();
        mRecentPDFList.addAll(DBHelper.queryRecentPDF());
        // 实时更新最新阅读列表
        if (event.isReadStateChange()) {
            mAdapter.notifyDataSetChanged();
        } else {
            mRvRecent.postDelayed(() -> mAdapter.notifyDataSetChanged(), 500);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        View layout = inflater.inflate(R.layout.app_fragment_recent, container, false);
        mUnbinder = ButterKnife.bind(this, layout);
        initView();
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        mUnbinder.unbind();
    }

    private void initView() {
        initData();

        mRvRecent.addItemDecoration(new XGridDecoration());
        mRvRecent.addItemDecoration(new YGridDecoration());
        RecyclerView.LayoutManager lm = new GridLayoutManager(mActivity, 3);
        mRvRecent.setLayoutManager(lm);
        mAdapter = new PDFAdapter(mRecentPDFList);
        mRvRecent.setAdapter(mAdapter);
    }

    private void initData() {
        mRecentPDFList.addAll(DBHelper.queryRecentPDF());
    }
}