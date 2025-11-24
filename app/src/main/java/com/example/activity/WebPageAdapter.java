package com.example.activity;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import helper.DatabaseHelper;
import helper.ItemTouchHelperAdapter;
import helper.ItemTouchHelperCallback;
import helper.WebPage;


public class WebPageAdapter extends RecyclerView.Adapter<WebPageAdapter.WebPageViewHolder>
        implements ItemTouchHelperAdapter {
    private DatabaseHelper databaseHelper; // 添加数据库帮助类对象
    private List<WebPage> webPages;
    private WebPageClickListener listener;
    private int selectedPosition = -1;
    private FloatingActionButton treeButton;
    private int INDENTATION_WIDTH = 10;
    private OnImageClickListener imageClickListener;

    private SparseBooleanArray selectedItems = new SparseBooleanArray(); // 用于跟踪选中的项目
    private boolean isSelectAll = false; // 用于存储全选状态
    private Button selectAllButton;
    private Button deleteButton;

    private RecyclerView recyclerView;


    public interface WebPageClickListener {
        void onWebPageClicked(WebPage webPage);
    }

    public WebPageAdapter(List<WebPage> webPages, WebPageClickListener listener, DatabaseHelper databaseHelper, OnImageClickListener imageClickListener, Button selectAllButton, Button deleteButton, RecyclerView recyclerView) {
        this.webPages = webPages;
        this.listener = listener;
        this.databaseHelper = databaseHelper;
        this.imageClickListener = imageClickListener;
        this.selectAllButton = selectAllButton; // Add this line
        this.deleteButton = deleteButton; // Add this line
        this.recyclerView = recyclerView;
        selectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSelectAll(); // 切换全选状态
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSelectedItems(); // 删除选中的项目
            }
        });



    }
    // 切换全选状态
    private void toggleSelectAll() {
        isSelectAll = !isSelectAll;
        for (int i = 0; i < getItemCount(); i++) {
            if (isSelectAll) {
                selectedItems.put(i, true);
            } else {
                selectedItems.delete(i);
            }
        }
        notifyDataSetChanged(); // 通知适配器数据已更改

        // 更新 CheckBox 的选中状态
        for (int i = 0; i < getItemCount(); i++) {
            if (isSelectAll) {
                webPages.get(i).setChecked(true); // 假设 WebPage 类有一个设置选中状态的方法
            } else {
                webPages.get(i).setChecked(false);
            }
        }
    }



    private void deleteSelectedItems() {

        List<WebPage> selectedItemsToMove = new ArrayList<>();
        List<Integer> selectedPositions = new ArrayList();

        // 遍历数据库中的项目，检查它们是否被选中
        for (int i = 0; i < webPages.size(); i++) {
            WebPage webPage = webPages.get(i);
            if (webPage.isChecked()) {
                selectedItemsToMove.add(webPage);
                selectedPositions.add(i);
            }
        }
        Log.d(TAG, "Deleted items count: " + selectedItemsToMove.size());
        Log.d(TAG, "Deleting " + selectedItemsToMove.size() + " items");


        // 从后往前删除，以防止删除后索引错位
        Collections.sort(selectedPositions, Collections.reverseOrder());

        if (!selectedItemsToMove.isEmpty()) {
            int selectedParentPosition = -1; // 选中项目的父项目位置
            if (!selectedPositions.isEmpty()) {
                selectedParentPosition = selectedPositions.get(0) - 1;
            }
            WebPage selectedParent = (selectedParentPosition >= 0) ? webPages.get(selectedParentPosition) : null;

            // 使用数据库帮助类移动选中项目的子项目到上一级或顶级，并删除选中项目
            databaseHelper.moveSelectedItemsAndDelete(selectedParent, selectedItemsToMove);

            // 清空选择状态
            selectedItems.clear();
            isSelectAll = false;

            notifyDataSetChanged();
            // 刷新 RecyclerView 的数据
            List<WebPage> webPages = buildTree(); // 构建树状页面结构
            updateData(webPages);
        }
    }


    private List<WebPage> buildTree() {
        List<WebPage> pages = new ArrayList<>();
        List<WebPage> topLevelPages = databaseHelper.getTopLevelWebPages(); // 获取顶级页面

        for (WebPage topLevelPage : topLevelPages) {
            topLevelPage.setLevel(0); // 顶级页面的级别为0
            pages.add(topLevelPage);
            buildSubTree(pages, topLevelPage, 1); // 构建子树
        }

        return pages;
    }
    private void buildSubTree(List<WebPage> pages, WebPage parentPage, int level) {
        List<WebPage> childPages = databaseHelper.getChildWebPages(parentPage.getId()); // 获取子页面

        for (WebPage childPage : childPages) {
            childPage.setLevel(level);
            pages.add(childPage);
            buildSubTree(pages, childPage, level + 1); // 递归构建子树
        }
    }


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        // 更新数据集中的项的顺序
        Collections.swap(webPages, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);

        // 更新数据库中的数据
        //updateParentIdsInDatabase(webPages);

        return true;
    }

    public void onItemDismiss(int position) {
        // 处理侧滑删除操作
        webPages.remove(position);
        notifyItemRemoved(position);
    }

    // 更新数据库中的父页面ID
    public void updateParentIdsInDatabase(List<WebPage> webPages) {
        for (int i = 0; i < webPages.size(); i++) {
            WebPage webPage = webPages.get(i);
            long parentId = (i > 0) ? webPages.get(i - 1).getId() : -1;
            webPage.setParentId(parentId);
            databaseHelper.updateParentId(webPage.getId(), parentId);
        }
    }

    @NonNull
    @Override
    public WebPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_webpage, parent, false);

        return new WebPageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull WebPageViewHolder holder, int position) {
        WebPage webPage = webPages.get(position);
        // 直接使用数据模型的状态来更新 CheckBox
        holder.checkBox.setChecked(webPage.isChecked());
        int indentation = INDENTATION_WIDTH * webPage.getLevel();
        // 增加缩进距离
        if (!webPageHasChildren(webPage)) {
            indentation += INDENTATION_WIDTH*8; // 增加一倍的缩进
        }

        holder.nodeContainer.setPadding(indentation, 0, 0, 0);
        holder.bind(webPage);

        if (selectedPosition == position) {
            holder.itemView.setBackgroundResource(R.drawable.blue_border_bg);
            //holder.textTitle.setBackgroundResource(R.drawable.gray_fill_bg);
        } else {
            holder.itemView.setBackgroundResource(0);
            holder.textTitle.setBackgroundResource(0);
        }
        Log.d(TAG, "onBindViewHolder called for position: " + position);

        // 根据是否有子页面设置 ImageView 的可见性
        if (webPageHasChildren(webPage)) {
            holder.arrowImg.setVisibility(View.VISIBLE);
        } else {
            holder.arrowImg.setVisibility(View.GONE);
        }
    }
    @Override
    public int getItemCount() {
        if (webPages == null) {
            return 0;
        }
        return webPages.size();
    }

    // 从数据库中获取页面数据
    private List<WebPage> getWebPagesFromDatabase(long parentId) {
        return databaseHelper.getAllWebPages();
    }

    // 判断页面是否有子页面
    private boolean webPageHasChildren(WebPage webPage) {
        return databaseHelper.webPageHasChildren(webPage);
    }
    public class WebPageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textTitle;
        private LinearLayout nodeContainer;
        private ImageView arrowImg;
        private AppCompatCheckBox checkBox;
        public WebPageViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            nodeContainer = itemView.findViewById(R.id.node_container);
            arrowImg = itemView.findViewById(R.id.arrow_img);
            checkBox = itemView.findViewById(R.id.checkBox);
            itemView.setOnClickListener(this);
            arrowImg.setOnClickListener(this);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        WebPage webPage = webPages.get(position);
                        webPage.setChecked(isChecked); // 更新数据模型中的选中状态
                    }
                }
            });



        }

        public void bind(WebPage webPage) {
            textTitle.setText(webPage.getTitle());
        }
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                if (v.getId() == R.id.arrow_img) {
                    Log.d(TAG, "Image clicked");
                    if (imageClickListener != null) {
                        Log.d(TAG, "Image clicked1");
                        WebPage webPage = webPages.get(position);
                        imageClickListener.onImageClicked(webPage);

                    }
                }
                else {
                    // 处理对项目本身的点击（如果需要的话）
                    selectedPosition = position;
                    notifyDataSetChanged();

                    if (listener != null) {
                        WebPage webPage = webPages.get(position);
                        listener.onWebPageClicked(webPage);
                    }
                }
            }
        }

    }
    public interface OnImageClickListener {
        void onImageClicked(WebPage webPage);
    }

    // 设置选中位置的方法
    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }

    // 更新适配器的数据
    public void updateData(List<WebPage> newData) {
        webPages.clear();
        webPages.addAll(newData);
        notifyDataSetChanged();
        Log.d(TAG, "updateData: "+newData);
    }

    public void highlightSelectedPage(String selectedUrl) {
        for (int i = 0; i < webPages.size(); i++) {
            WebPage webPage = webPages.get(i);

            if (webPage.getUrl().equals(selectedUrl)) {
                setSelectedPosition(i);
                notifyDataSetChanged();
                PhoneConnectionService.sendWebPageDataToWatch(webPage,true);

                // 滚动到选中的位置
                recyclerView.smoothScrollToPosition(i);
                break;
            }
        }
    }


    // 切换arrowImg的图像和展开/收缩状态
    public void toggleChildren(WebPage webPage) {
        if (webPageHasChildren(webPage)) {
            int position = webPages.indexOf(webPage);
            if (position != -1) {
                WebPageViewHolder holder = (WebPageViewHolder) recyclerView.findViewHolderForAdapterPosition(position);

                if (holder != null) {
                    // 如果webPage在webPages列表中存在
                    List<WebPage> childPages = databaseHelper.getAllChildWebPages(webPage);

                    if (webPages.containsAll(childPages)) {
                        // 已展开，收缩子页面
                        webPages.removeAll(childPages);
                        holder.arrowImg.setImageResource(R.drawable.ic_keyboard_arrow_right_white_24px);
                    } else {
                        // 未展开，展开子页面
                        int insertPosition = position + 1;
                        for (WebPage childPage : childPages) {
                            // 设置子页面的缩进
                            childPage.setLevel(webPage.getLevel() + 1);
                            webPages.add(insertPosition, childPage);
                            insertPosition++;
                        }
                    }

                    // 更新webPage的展开状态
                    webPage.setExpanded(!webPages.containsAll(childPages));
                    // 切换箭头图标为向下
                    holder.arrowImg.setImageResource(R.drawable.ic_keyboard_arrow_down_white_24px);
                    // 刷新RecyclerView显示
                    notifyDataSetChanged();
                }
            }
        }
    }





}