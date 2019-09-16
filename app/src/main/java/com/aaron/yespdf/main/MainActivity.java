package com.aaron.yespdf.main;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.aaron.base.impl.OnClickListenerImpl;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.about.AboutActivity;
import com.aaron.yespdf.common.CommonActivity;
import com.aaron.yespdf.common.DBHelper;
import com.aaron.yespdf.common.UiManager;
import com.aaron.yespdf.common.bean.PDF;
import com.aaron.yespdf.common.event.AllEvent;
import com.aaron.yespdf.common.event.HotfixEvent;
import com.aaron.yespdf.common.utils.DialogUtils;
import com.aaron.yespdf.filepicker.SelectActivity;
import com.aaron.yespdf.settings.SettingsActivity;
import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.github.mmin18.widget.RealtimeBlurView;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends CommonActivity implements IMainContract.V, IAllAdapterComm {

    private static final int SELECT_REQUEST_CODE = 101;

    @BindView(R2.id.app_tab_layout) TabLayout mTabLayout;
    @BindView(R2.id.app_vp) ViewPager mVp;
    @BindView(R2.id.app_blur_view) RealtimeBlurView mBlurView;

    private IMainContract.P mP;

    private Unbinder mUnbinder;
    private Dialog mLoadingDialog;
    private PopupWindow mPwMenu;
    private PopupWindow mPwCollection;

    private TextView mTvCollectionTitle;
    private RecyclerView mRvCollection;
    private RecyclerView.Adapter mCollectionAdapter;

    private List<PDF> mPDFList = new ArrayList<>();

    @Override
    protected int layoutId() {
        return R.layout.app_activity_main;
    }

    @Override
    protected Toolbar createToolbar() {
        return findViewById(R.id.app_toolbar);
    }

    /**
     * 只是为了在打开文档时关闭 PopupWindow
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAllEvent(AllEvent event) {
        if (mPwCollection.isShowing()) {
            mRvCollection.postDelayed(() -> mPwCollection.dismiss(), 500);
        }
    }

    /**
     * 热修复完成，提示用户重启应用
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onHotfixSuccess(HotfixEvent event) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.app_dialog_double_btn, null);
        Dialog hotfixDialog = DialogUtils.createDialog(this, dialogView);
        hotfixDialog.setCanceledOnTouchOutside(false);
        TextView tvTitle = dialogView.findViewById(R.id.app_tv_title);
        TextView tvContent = dialogView.findViewById(R.id.app_tv_content);
        Button btnLeft = dialogView.findViewById(R.id.app_btn_left);
        Button btnRight = dialogView.findViewById(R.id.app_btn_right);
        tvTitle.setText(R.string.app_find_update);
        tvContent.setText(R.string.app_restart_to_update);
        btnLeft.setText(R.string.app_later);
        btnRight.setText(R.string.app_restart_right_now);
        btnLeft.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                hotfixDialog.dismiss();
            }
        });
        btnRight.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        });
        hotfixDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attachP();
        EventBus.getDefault().register(this);
        mUnbinder = ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mUnbinder.unbind();
        mP.detachV();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_REQUEST_CODE && resultCode == RESULT_OK) {
            mP.insertPDF(data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.app_more) {
            View parent = getWindow().getDecorView();
            int x = ConvertUtils.dp2px(6);
            int y = ConvertUtils.dp2px(80);
            mPwMenu.showAtLocation(parent, Gravity.TOP | Gravity.END, x, y);
        }
        return true;
    }

    @Override
    public void onTap(String name) {
        mTvCollectionTitle.setText(name);
        mPDFList.clear();
        mPDFList.addAll(DBHelper.queryPDF(name));
        mCollectionAdapter.notifyDataSetChanged();
        mBlurView.animate().alpha(1).setDuration(200).start();
        mPwCollection.showAtLocation(mVp, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onShowMessage(int stringId) {
        UiManager.showShort(stringId);
    }

    @Override
    public void onShowLoading() {
        mLoadingDialog.show();
    }

    @Override
    public void onHideLoading() {
        mLoadingDialog.dismiss();
    }

    @Override
    public void onUpdate() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f instanceof IAllFragmentComm) {
                ((IAllFragmentComm) f).update();
                break;
            }
        }
    }

    @Override
    public void attachP() {
        mP = new MainP(this);
    }

    private void initView() {
        mLoadingDialog = DialogUtils.createDialog(this, R.layout.app_dialog_loading);

        initPwMenu();
        initPwCollection();

        mTabLayout.setupWithViewPager(mVp);
        FragmentPagerAdapter fragmentPagerAdapter = new MainFragmentAdapter(getSupportFragmentManager());
        mVp.setAdapter(fragmentPagerAdapter);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void initPwMenu() {
        @SuppressLint("InflateParams")
        View pwView = LayoutInflater.from(this).inflate(R.layout.app_pw_main, null);
        TextView tvImport   = pwView.findViewById(R.id.app_tv_import);
        TextView tvSettings = pwView.findViewById(R.id.app_tv_settings);
        TextView tvAbout    = pwView.findViewById(R.id.app_tv_about);
        mPwMenu = new PopupWindow(pwView);
        tvImport.setOnClickListener(v -> {
            PermissionUtils.permission(PermissionConstants.STORAGE)
                    .callback(new PermissionUtils.SimpleCallback() {
                        @Override
                        public void onGranted() {
                            ArrayList<String> imported = new ArrayList<>();
                            List<PDF> pdfList = DBHelper.queryAllPDF();
                            for (PDF pdf : pdfList) {
                                imported.add(pdf.getPath());
                            }
                            SelectActivity.start(MainActivity.this, SELECT_REQUEST_CODE, imported);
                        }

                        @Override
                        public void onDenied() {
                            UiManager.showShort(R.string.app_have_no_storage_permission);
                        }
                    })
                    .request();
            mPwMenu.dismiss();
        });
        tvSettings.setOnClickListener(v -> {
            SettingsActivity.start(this);
            mPwMenu.dismiss();
        });
        tvAbout.setOnClickListener(v -> {
            AboutActivity.start(this);
            mPwMenu.dismiss();
        });
        mPwMenu.setAnimationStyle(R.style.AppPwMenu);
        mPwMenu.setFocusable(true);
        mPwMenu.setOutsideTouchable(true);
        mPwMenu.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMenu.setElevation(ConvertUtils.dp2px(4));
    }

    private void initPwCollection() {
        @SuppressLint("InflateParams")
        View pwView = LayoutInflater.from(this).inflate(R.layout.app_pw_collection, null);
        mTvCollectionTitle = pwView.findViewById(R.id.app_tv_title);
        mRvCollection = pwView.findViewById(R.id.app_rv_collection);
        mRvCollection.addItemDecoration(new XGridDecoration());
        mRvCollection.addItemDecoration(new YGridDecoration());
        RecyclerView.LayoutManager lm = new GridLayoutManager(this, 3);
        mRvCollection.setLayoutManager(lm);

        mCollectionAdapter = new PDFAdapter(mPDFList, false);
        mRvCollection.setAdapter(mCollectionAdapter);
        mPwCollection = new PopupWindow(pwView);
        mPwCollection.setOnDismissListener(() -> {
            mRvCollection.scrollToPosition(0);
            mBlurView.animate().alpha(0).setDuration(200).start();
        });

        mPwCollection.setAnimationStyle(R.style.AppPwCollection);
        mPwCollection.setFocusable(true);
        mPwCollection.setOutsideTouchable(true);
        mPwCollection.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPwCollection.setHeight(ConvertUtils.dp2px(500));
        mPwCollection.setElevation(ConvertUtils.dp2px(2));
    }
}
