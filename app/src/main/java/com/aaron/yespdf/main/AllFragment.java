package com.aaron.yespdf.main;

import android.graphics.Rect;
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
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class AllFragment extends BaseFragment {

    @BindView(R2.id.app_rv_all) RecyclerView mRvAll;

    private Unbinder mUnbinder;

    static Fragment newInstance() {
        return new AllFragment();
    }

    public AllFragment() {

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
        mRvAll.addItemDecoration(new XItemDecoration());
        mRvAll.addItemDecoration(new YItemDecoration());
        RecyclerView.LayoutManager lm = new GridLayoutManager(mActivity, 3);
        mRvAll.setLayoutManager(lm);
        RecyclerView.Adapter adapter = new AllAdapter();
        mRvAll.setAdapter(adapter);
    }

    private static class XItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int pos = parent.getChildAdapterPosition(view);
            switch (pos % 3) {
                case 0:
                case 1:
                case 2:
                    outRect.left = ConvertUtils.dp2px(8);
                    outRect.right = ConvertUtils.dp2px(8);
                    break;
            }
        }
    }

    private static class YItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) < 3) {
                outRect.top = ConvertUtils.dp2px(8);
            } else {
                outRect.top = ConvertUtils.dp2px(24);
            }
        }
    }
}
