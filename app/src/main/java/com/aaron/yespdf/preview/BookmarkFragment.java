package com.aaron.yespdf.preview;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.base.BaseFragment;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.blankj.utilcode.util.LogUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class BookmarkFragment extends BaseFragment implements IBkFragComm {

    @BindView(R2.id.app_rv_bookmark) RecyclerView mRvBookmark;

    private Unbinder mUnbinder;
    private RecyclerView.Adapter mAdapter;

    private List<Bookmark> mBookmarks = new ArrayList<>();

    static Fragment newInstance() {
        return new BookmarkFragment();
    }

    @Override
    public void update(Collection<Bookmark> collection) {
        mBookmarks.clear();
        mBookmarks.addAll(collection);
        Collections.sort(mBookmarks, (bk1, bk2) -> (int) (bk2.getTime() - bk1.getTime()));
        mAdapter.notifyDataSetChanged();
        LogUtils.e(mBookmarks);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_fragment_bookmark, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void initView() {
        LinearLayoutManager lm = new LinearLayoutManager(mActivity);
        mRvBookmark.setLayoutManager(lm);
        DividerItemDecoration decoration = new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(new ColorDrawable(getResources().getColor(R.color.base_black_divider)));
        mRvBookmark.addItemDecoration(decoration);
        mAdapter = new BookmarkAdapter(mBookmarks);
        mRvBookmark.setAdapter(mAdapter);
    }
}
