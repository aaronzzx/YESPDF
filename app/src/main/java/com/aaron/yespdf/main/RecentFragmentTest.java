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
import com.blankj.utilcode.util.LogUtils;

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
public class RecentFragmentTest extends BaseFragment implements IOperation, AbstractAdapter.ICommInterface<PDF> {

    @BindView(R2.id.app_rv_recent)
    RecyclerView rvRecent;

    private Unbinder unbinder;
    private AbstractAdapter<PDF> adapter;

    private List<PDF> recentPDFList = new ArrayList<>();

    static Fragment newInstance() {
        return new RecentFragmentTest();
    }

    public RecentFragmentTest() {

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
    public void onResume() {
        super.onResume();
        ((MainActivityTest) mActivity).setOperation(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
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

    @Override
    public void onStartOperation() {
        ((MainActivityTest) mActivity).showOperationBar();
    }

    @Override
    public void onSelect(List<PDF> list, boolean selectAll) {
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
        adapter = new RecentAdapterTest(this, recentPDFList);
        rvRecent.setAdapter(adapter);
    }

    private void initData() {
        recentPDFList.addAll(DBHelper.queryRecentPDF());
    }
}
