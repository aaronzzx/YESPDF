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
import com.blankj.utilcode.util.LogUtils;
import com.shockwave.pdfium.PdfDocument;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class ContentFragment extends BaseFragment {

//    @BindView(R2.id.app_rv_content) RecyclerView mRvContent;
    @BindView(R2.id.app_ll) LinearLayout mLl;

    private Unbinder mUnbinder;
    private static List<PdfDocument.Bookmark> sBookmarkList;
//    private TreeViewAdapter mTreeViewAdapter;

//    private static List<TreeNode> sTreeNodes = new ArrayList<>();

    static Fragment newInstance(List<PdfDocument.Bookmark> list) {
        Fragment fragment = new ContentFragment();
        sBookmarkList = list;
//        initContent(null, list);
        return fragment;
    }

//    private static void initContent(TreeNode<Content> parent, List<PdfDocument.Bookmark> childList) {
//        for (PdfDocument.Bookmark bk : childList) {
//            int icon = bk.hasChildren() ? R.drawable.app_ic_can_down_grey : R.drawable.app_ic_cannot_down_grey;
//            String title = bk.getTitle();
//            String pageId = String.valueOf(bk.getPageIdx() + 1);
//            TreeNode<Content> node = new TreeNode<>(new Content(icon, title, pageId));
//            if (bk.hasChildren()) {
//                initContent(node, bk.getChildren());
//            }
//            if (parent != null) {
//                parent.addChild(node);
//            } else {
//                sTreeNodes.add(node);
//            }
//        }
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.app_fragment_content, container, false);
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
        if (mActivity == null) return;

        initContent(sBookmarkList);

//        RecyclerView.LayoutManager lm = new LinearLayoutManager(mActivity);
//        mRvContent.setLayoutManager(lm);
//        List<TreeViewBinder> binderList = new ArrayList<>();
//        binderList.add(new ContentBinder());
//        mTreeViewAdapter = new TreeViewAdapter(sTreeNodes, binderList);
//        mTreeViewAdapter.setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
//            @Override
//            public boolean onClick(TreeNode node, RecyclerView.ViewHolder viewHolder) {
//                if (!node.isLeaf()) {
//                    // Update and toggle the node.
//                    onToggle(node.isExpand(), viewHolder);
//                }
//                return false;
//            }
//
//            @Override
//            public void onToggle(boolean isExpand, RecyclerView.ViewHolder viewHolder) {
//                LogUtils.e(isExpand);
//                ContentBinder.ViewHolder holder = (ContentBinder.ViewHolder) viewHolder;
//                int degree = isExpand ? 0 : 90;
//                holder.getIvIcon()
//                        .animate()
//                        .rotation(degree)
//                        .start();
//            }
//        });
//        mRvContent.setAdapter(mTreeViewAdapter);
    }

    private void initContent(List<PdfDocument.Bookmark> list) {
        for (PdfDocument.Bookmark bk : list) {
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
                    ((ICommunicable) mActivity).onJumpTo(pageId);
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
                    ((ICommunicable) mActivity).onJumpTo(pageId);
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
