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
public class AllFragment extends BaseFragment implements IAllFragInterface, IOperationInterface {

    @BindView(R2.id.app_rv_all)
    RecyclerView rvAll;

    private Unbinder unbinder;
    private RecyclerView.Adapter adapter;

    private List<Collection> collections = new ArrayList<>();

    static Fragment newInstance() {
        return new AllFragment();
    }

    public AllFragment() {

    }

    @Override
    public void update() {
        collections.clear();
        collections.addAll(DBHelper.queryAllCollection());
        adapter.notifyItemRangeChanged(0, collections.size(), 0);
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
    public void onDestroyView() {
        super.onDestroyView();
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
        LogUtils.e(flag);
        if (isResumed()) {
            LogUtils.e("inner: " + flag);
            ((IOperationInterface) adapter).selectAll(flag);
        }
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
        adapter = new AllAdapter(collections);
        rvAll.setAdapter(adapter);
    }
}
