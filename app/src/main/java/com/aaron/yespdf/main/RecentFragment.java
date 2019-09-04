package com.aaron.yespdf.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aaron.base.base.BaseFragment;
import com.aaron.yespdf.R;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class RecentFragment extends BaseFragment {

    static Fragment newInstance() {
        return new RecentFragment();
    }

    public RecentFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.app_fragment_recent, container, false);
    }
}
