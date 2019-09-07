package com.aaron.yespdf.about;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.BuildConfig;
import com.aaron.yespdf.R;
import com.aaron.yespdf.common.CommonActivity;
import com.github.anzewei.parallaxbacklayout.ParallaxBack;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

@ParallaxBack
public class AboutActivity extends CommonActivity implements IAboutContract.V<Message, Library> {

    private Unbinder mUnbinder;
    private IAboutContract.P mPresenter;

    @BindView(R.id.app_rv_message) RecyclerView mRvMessage;
    @BindView(R.id.app_rv_library) RecyclerView mRvLibrary;

    public static void start(Context context) {
        Intent starter = new Intent(context, AboutActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int layoutId() {
        return R.layout.app_activity_about;
    }

    @Override
    protected Toolbar createToolbar() {
        return findViewById(R.id.app_toolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUnbinder = ButterKnife.bind(this);
        initView();
        attachPresenter();
        mPresenter.requestMessage(AboutPresenter.Element.ICON_ID, AboutPresenter.Element.TITLE);
        mPresenter.requestLibrary(AboutPresenter.Element.LIBRARY_NAME,
                AboutPresenter.Element.LIBRARY_AUTHOR,
                AboutPresenter.Element.LIBRARY_INTRODUCE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
        mUnbinder.unbind();
    }

    /**
     * 标题栏返回键销毁活动
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void attachPresenter() {
        mPresenter = new AboutPresenter(this);
    }

    @Override
    public void onShowMessage(List<Message> list) {
        initMessage(list);
    }

    @Override
    public void onShowLibrary(List<Library> list) {
        initLibrary(list);
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        initToolbar();
        initVersionStatus();
    }

    private void initToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.app_ic_action_back_black);
        }
        mToolbar.setTitle(R.string.app_about);
    }

    @SuppressLint("SetTextI18n")
    private void initVersionStatus() {
        TextView version = findViewById(R.id.app_tv_version);
        String versionName = BuildConfig.VERSION_NAME;
        version.setText("Version" + versionName);
    }

    private <T> void initMessage(List<T> messageList) {
        LinearLayoutManager messageManager = new LinearLayoutManager(this);
        mRvMessage.setLayoutManager(messageManager);
        MessageAdapter messageAdapter = new MessageAdapter<>(this, messageList);
        mRvMessage.setAdapter(messageAdapter);
    }

    private <T> void initLibrary(List<T> libraryList) {
        LinearLayoutManager libraryManager = new LinearLayoutManager(this);
        mRvLibrary.setLayoutManager(libraryManager);
        LibraryAdapter libraryAdapter = new LibraryAdapter<>(this, libraryList);
        mRvLibrary.setAdapter(libraryAdapter);
    }
}
