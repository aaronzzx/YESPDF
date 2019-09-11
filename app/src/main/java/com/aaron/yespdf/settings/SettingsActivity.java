package com.aaron.yespdf.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.CommonActivity;
import com.github.anzewei.parallaxbacklayout.ParallaxBack;
import com.github.anzewei.parallaxbacklayout.ParallaxHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

@ParallaxBack
public class SettingsActivity extends CommonActivity implements IComm {

    private static final String EXTRA_FROM_PREVIEW = "EXTRA_FROM_PREVIEW";

    @BindView(R2.id.app_rv_settings) RecyclerView mRvSettings;

    private Unbinder mUnbinder;
    private boolean mColorReverseChange;

    public static void start(Context context) {
        Intent starter = new Intent(context, SettingsActivity.class);
        context.startActivity(starter);
    }

    public static void start(Activity activity, int requestCode) {
        Intent starter = new Intent(activity, SettingsActivity.class);
        starter.putExtra(EXTRA_FROM_PREVIEW, true);
        activity.startActivityForResult(starter, requestCode);
    }

    @Override
    protected int layoutId() {
        return R.layout.app_activity_settings;
    }

    @Override
    protected Toolbar createToolbar() {
        return findViewById(R.id.app_toolbar);
    }

    @Override
    public void onColorReverse(boolean change) {
        mColorReverseChange = change;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUnbinder = ButterKnife.bind(this);

        Intent data = getIntent();
        disableSwipeBack(data != null && data.getBooleanExtra(EXTRA_FROM_PREVIEW, false));

        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mColorReverseChange) {
            setResult(RESULT_OK);
        }
        finish();
    }

    private void disableSwipeBack(boolean disable) {
        if (disable) {
            ParallaxHelper.disableParallaxBack(this);
        }
    }

    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.app_ic_action_back_black);
        }
        mToolbar.setTitle(R.string.app_settings);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        mRvSettings.setLayoutManager(lm);
        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(new ColorDrawable(getResources().getColor(R.color.base_black_divider)));
        mRvSettings.addItemDecoration(decoration);
        RecyclerView.Adapter adapter = new SettingsAdapter();
        mRvSettings.setAdapter(adapter);
    }
}
