package com.aaron.yespdf.main;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.UiManager;
import com.aaron.yespdf.common.bean.PDF;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.github.mmin18.widget.RealtimeBlurView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class CollectionFragmentTest extends DialogFragment implements IOperation, AbstractAdapter.ICommInterface<PDF> {

    private static final String BUNDLE_NAME = "BUNDLE_NAME";

    @BindView(R2.id.app_blur_view)
    RealtimeBlurView realtimeBlurView;
    @BindView(R2.id.app_vg_operation)
    ViewGroup vgOperationBar;
    @BindView(R2.id.app_ibtn_cancel)
    ImageButton ibtnCancel;
    @BindView(R2.id.app_tv_title)
    TextView tvTitle;
    @BindView(R2.id.app_ibtn_delete)
    ImageButton ibtnDelete;
    @BindView(R2.id.app_ibtn_select_all)
    ImageButton ibtnSelectAll;
    @BindView(R2.id.app_tv_name)
    TextView tvName;
    @BindView(R2.id.app_rv_collection)
    RecyclerView rvCollection;

    private Unbinder unbinder;
    private AbstractAdapter adapter;

    private String name;
    private float translationY;
    private List<PDF> pdfList = new ArrayList<>();
    private List<PDF> selectPDFList;

    static DialogFragment newInstance(String name) {
        DialogFragment fragment = new CollectionFragmentTest();
        Bundle args = new Bundle();
        args.putString(BUNDLE_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.AppCollectionFragment);
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.dimAmount = 0.0F;
        lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setWindowAnimations(R.style.AppPwCollection);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
//        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_fragment_collection, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 监听返回键
        View view = getView();
        if (view != null) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                        if (vgOperationBar.getVisibility() == View.VISIBLE) {
                            OperationBarHelper.hide(vgOperationBar, translationY);
                            adapter.cancelSelect();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStartOperation() {
        tvTitle.setText(getString(R.string.app_selected_zero));
        ibtnSelectAll.setSelected(false);
        OperationBarHelper.show(vgOperationBar);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSelect(List<PDF> list, boolean selectAll) {
        LogUtils.e(list);
        ibtnSelectAll.setSelected(selectAll);
        tvTitle.setText(getString(R.string.app_selected) + "(" + list.size() + ")");
        selectPDFList = list;
    }

    @Override
    public void delete() {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<Object>() {
            @Nullable
            @Override
            public Object doInBackground() {
                pdfList.removeAll(selectPDFList);
                DBHelper.deletePDF(selectPDFList);
                return null;
            }

            @Override
            public void onSuccess(@Nullable Object result) {
                UiManager.showShort(R.string.app_delete_completed);
                adapter.notifyItemRangeChanged(0, pdfList.size(), 0);
            }
        });
    }

    @Override
    public void selectAll(boolean selectAll) {
        ibtnSelectAll.setSelected(selectAll);
        adapter.selectAll(selectAll);
    }

    @Override
    public void cancelSelect() {
        OperationBarHelper.hide(vgOperationBar, translationY);
        adapter.cancelSelect();
    }

    @Override
    public void update() {

    }

    private void initView() {
        Bundle args = getArguments();

        if (args != null) {
            name = args.getString(BUNDLE_NAME);
            tvName.setText(name);
            pdfList.addAll(DBHelper.queryPDF(name));
        }

        vgOperationBar.post(() -> translationY = vgOperationBar.getTranslationY());

        setListener();

        rvCollection.addItemDecoration(new XGridDecoration());
        rvCollection.addItemDecoration(new YGridDecoration());
        RecyclerView.LayoutManager lm = new GridLayoutManager(getActivity(), 3);
        rvCollection.setLayoutManager(lm);

        adapter = new CollectionAdapterTest(this, pdfList);
        rvCollection.setAdapter(adapter);
    }

    private void setListener() {
        realtimeBlurView.setOnClickListener(v -> dismiss());
        ibtnCancel.setOnClickListener(v -> cancelSelect());
        ibtnDelete.setOnClickListener(v -> delete());
        ibtnSelectAll.setOnClickListener(v -> selectAll(!v.isSelected()));
    }
}
