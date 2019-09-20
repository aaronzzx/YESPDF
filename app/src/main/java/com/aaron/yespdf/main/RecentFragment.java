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
import com.aaron.yespdf.common.event.MaxRecentEvent;
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
public class RecentFragment extends BaseFragment implements IOperationInterface {

    @BindView(R2.id.app_rv_recent)
    RecyclerView rvRecent;

    private Unbinder unbinder;
    private RecyclerView.Adapter adapter;

    private List<PDF> recentPDFList = new ArrayList<>();

    static Fragment newInstance() {
        return new RecentFragment();
    }

    public RecentFragment() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecentPDFEvent(RecentPDFEvent event) {
        recentPDFList.clear();
        recentPDFList.addAll(DBHelper.queryRecentPDF());
        // 实时更新最新阅读列表
        if (event.isFromPreviewActivity()) {
            // 由 PreviewActivity 发出而接收
            adapter.notifyDataSetChanged();
        } else {
            // 由于还在 MainActivity 界面，所以不立即更新界面
            rvRecent.postDelayed(() -> adapter.notifyDataSetChanged(), 500);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMaxRecentEvent(MaxRecentEvent event) {
        adapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        View layout = inflater.inflate(R.layout.app_fragment_recent, container, false);
        unbinder = ButterKnife.bind(this, layout);
        initView();
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
    }

    @Override
    public void cancel() {
        if (isResumed()) {
            ((IOperationInterface) adapter).cancel();
        }
    }

    @Override
    public void selectAll(boolean flag) {
        if (isResumed()) {
            ((IOperationInterface) adapter).selectAll(flag);
        }
    }

    private void initView() {
        initData();

        rvRecent.setItemAnimator(null);
        rvRecent.addItemDecoration(new XGridDecoration());
        rvRecent.addItemDecoration(new YGridDecoration());
        GridLayoutManager lm = new GridLayoutManager(mActivity, 3);
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (recentPDFList.isEmpty()) {
                    return 3;
                }
                return 1;
            }
        });
        rvRecent.setLayoutManager(lm);
        adapter = new RecentPDFAdapter(recentPDFList);
        rvRecent.setAdapter(adapter);
    }

    private void initData() {
        recentPDFList.addAll(DBHelper.queryRecentPDF());
    }
}
