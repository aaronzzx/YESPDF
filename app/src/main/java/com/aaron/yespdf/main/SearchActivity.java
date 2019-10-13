package com.aaron.yespdf.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.impl.TextWatcherImpl;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.CommonActivity;
import com.aaron.yespdf.common.DataManager;
import com.aaron.yespdf.common.XGridDecoration;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.event.RecentPDFEvent;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.github.anzewei.parallaxbacklayout.ParallaxBack;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

@ParallaxBack
public class SearchActivity extends CommonActivity {

    @BindView(R2.id.app_ibtn_back)
    ImageButton ibtnBack;
    @BindView(R2.id.app_et_search)
    EditText etSearch;
    @BindView(R2.id.app_ibtn_inverse)
    ImageButton ibtnInverse;
    @BindView(R2.id.app_ibtn_clear)
    ImageButton ibtnClear;
    @BindView(R2.id.app_rv)
    RecyclerView rv;

    private Unbinder unbinder;
    private SearchAdapter adapter;

    public static void start(Context context) {
        Intent starter = new Intent(context, SearchActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int layoutId() {
        return R.layout.app_activity_search;
    }

    @Override
    protected Toolbar createToolbar() {
        return findViewById(R.id.app_toolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        unbinder = ButterKnife.bind(this);
        initToolbar();
        initView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (KeyboardUtils.isSoftInputVisible(this)) {
            KeyboardUtils.hideSoftInput(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecentPDFEvent(RecentPDFEvent event) {
        adapter.update();
        // 实时更新最新阅读列表
        if (event.isFromPreviewActivity()) {
            // 由 PreviewActivity 发出而接收
            adapter.getFilter().filter(etSearch.getText());
        }
    }

    private void initView() {
        List<PDF> pdfList = DataManager.getPdfList();

        ibtnBack.setOnClickListener(v -> finish());
        ibtnInverse.setOnClickListener(v -> {
            if (KeyboardUtils.isSoftInputVisible(this)) {
                KeyboardUtils.hideSoftInput(this);
            }
            ibtnInverse.setSelected(!ibtnInverse.isSelected());
            adapter.setInverse(ibtnInverse.isSelected());
            adapter.getFilter().filter(etSearch.getText());
        });
        ibtnClear.setOnClickListener(v -> {
            etSearch.setText("");
            if (!KeyboardUtils.isSoftInputVisible(this)) {
                KeyboardUtils.showSoftInput(this);
            }
        });
        etSearch.addTextChangedListener(new TextWatcherImpl() {
            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {
                ibtnInverse.setVisibility(c.length() == 0 ? View.GONE : View.VISIBLE);
                ibtnClear.setVisibility(c.length() == 0 ? View.GONE : View.VISIBLE);
                adapter.getFilter().filter(c);
            }
        });

        rv.addItemDecoration(new XGridDecoration());
        rv.addItemDecoration(new YItemDecoration());
        GridLayoutManager lm = new GridLayoutManager(this, 3);
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.isEmpty() || position == 0) {
                    return 3;
                }
                return 1;
            }
        });
        rv.setLayoutManager(lm);
        adapter = new SearchAdapter(pdfList, false);
        rv.setAdapter(adapter);
    }

    private void initToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private static class YItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int pos = parent.getChildAdapterPosition(view);
            if (pos == 0) return;
            outRect.bottom = ConvertUtils.dp2px(24);
        }
    }
}
