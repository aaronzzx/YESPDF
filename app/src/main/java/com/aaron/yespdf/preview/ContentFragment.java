package com.aaron.yespdf.preview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aaron.base.base.BaseFragment;
import com.aaron.yespdf.R;
import com.aaron.yespdf.R2;
import com.aaron.yespdf.common.widgets.ImageTextView;
import com.shockwave.pdfium.PdfDocument;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class ContentFragment extends BaseFragment implements IContetnFragComm {

    @BindView(R2.id.app_ll) LinearLayout mLl;
    @BindView(R2.id.app_itv_placeholder)
    ImageTextView mItvEmpty;

    private Unbinder mUnbinder;
    private List<PdfDocument.Bookmark> mContentList = new ArrayList<>();

    static Fragment newInstance() {
        return new ContentFragment();
    }

    @Override
    public void update(Collection<PdfDocument.Bookmark> collection) {
        mContentList.clear();
        mContentList.addAll(collection);
        initContent();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.app_fragment_content, container, false);
        mUnbinder = ButterKnife.bind(this, layout);
        mItvEmpty.setText(R.string.app_have_no_content);
        mItvEmpty.setIconTop(R.drawable.app_ic_content_emptyview);
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void initContent() {
        if (!mContentList.isEmpty()) {
            mItvEmpty.setVisibility(View.GONE);
            for (PdfDocument.Bookmark bk : mContentList) {
                int icon = bk.hasChildren() ? R.drawable.app_ic_can_down_grey : R.drawable.app_ic_cannot_down_grey;
                String title = bk.getTitle();
                String pageId = String.valueOf(bk.getPageIdx() + 1);

                //创建根节点
                TreeNode root = TreeNode.root();

                //创建节点item
                ContentHolder.IconTreeItem iconTreeItem = new ContentHolder.IconTreeItem(icon, title, pageId);

                // 点击监听
                ContentHolder holder = new ContentHolder(mActivity);
                holder.setOnIconTapListener(new ContentHolder.OnIconTapListener() {
                    @Override
                    public void onIconTap(TreeNode node) {
                        holder.getTreeView().toggleNode(node);
                    }
                });
                holder.setOnNodeTapListener(new ContentHolder.OnNodeTapListener() {
                    @Override
                    public void onNodeTap(int pageId) {
                        ((IActivityComm) mActivity).onJumpTo(pageId);
                    }
                });

                TreeNode cur = new TreeNode(iconTreeItem).setViewHolder(holder);
                if (bk.hasChildren()) {
                    addChild(cur, bk.getChildren());
                }
                root.addChild(cur);
                //创建树形视图
                AndroidTreeView treeView = new AndroidTreeView(mActivity, root);
                //设置树形视图开启默认动画
                treeView.setDefaultAnimation(false);
                //设置树形视图默认的样式
                treeView.setDefaultContainerStyle(R.style.AppTreeNode);
                //设置树形视图默认的ViewHolder
                treeView.setDefaultViewHolder(ContentHolder.class);
                //将树形视图添加到layout中
                mLl.addView(treeView.getView());
            }
        } else {
            mItvEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void addChild(TreeNode parent, List<PdfDocument.Bookmark> childList) {
        for (PdfDocument.Bookmark bk : childList) {
            int icon = bk.hasChildren() ? R.drawable.app_ic_can_down_grey : R.drawable.app_ic_cannot_down_grey;
            String title = bk.getTitle();
            String pageId = String.valueOf(bk.getPageIdx() + 1);

            //创建节点item
            ContentHolder.IconTreeItem iconTreeItem = new ContentHolder.IconTreeItem(icon, title, pageId);

            // 点击监听
            // 点击监听
            ContentHolder holder = new ContentHolder(mActivity);
            holder.setOnIconTapListener(new ContentHolder.OnIconTapListener() {
                @Override
                public void onIconTap(TreeNode node) {
                    holder.getTreeView().toggleNode(node);
                }
            });
            holder.setOnNodeTapListener(new ContentHolder.OnNodeTapListener() {
                @Override
                public void onNodeTap(int pageId) {
                    ((IActivityComm) mActivity).onJumpTo(pageId);
                }
            });

            TreeNode cur = new TreeNode(iconTreeItem).setViewHolder(holder);
            if (bk.hasChildren()) {
                addChild(cur, bk.getChildren());
            }
            parent.addChild(cur);
        }
    }
}
