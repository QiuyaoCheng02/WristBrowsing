package com.example.activity;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import helper.DatabaseHelper;
import helper.ItemTouchHelperCallback;
import helper.WebPage;

public class treeActivity extends BaseActivity implements WebPageAdapter.OnImageClickListener{

    private RecyclerView recyclerView;
    private WebPageAdapter adapter;
    private List<WebPage> webPages = new ArrayList<>();
    private List<WebPage> webPages1 = new ArrayList<>();
    private WebPage webPageToOpen;
    private boolean isDataLoaded = false;
    private DatabaseHelper databaseHelper; // 添加数据库帮助类对象

    private Button newPage;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree);

        databaseHelper = new DatabaseHelper(this);
        newPage=findViewById(R.id.btnAddLink);
        Button selectAllButton = findViewById(R.id.btnSelectAll);
        Button deleteButton = findViewById(R.id.btnDelLink);
        //parentId = getIntent().getLongExtra("parentId", -1);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 在 onCreate 中初始化数
        //webPages.addAll(buildTree());
        List<WebPage> webPages = buildTree(); // 构建树状页面结构
        //webPages = getWebPagesFromDatabase(); // 从数据库中获取页面数据
        //webPages=databaseHelper.getWebPagesByParentId(10);
        // 初始化 WebPageAdapter 时传递 recyclerView
        adapter = new WebPageAdapter(webPages, new WebPageAdapter.WebPageClickListener() {
            @Override
            public void onWebPageClicked(WebPage webPage) {
                PhoneConnectionService.sendWebPageDataToWatch(webPage, false);
            }
        }, databaseHelper, this, selectAllButton, deleteButton, recyclerView);

        recyclerView.setAdapter(adapter);

        // 创建 ItemTouchHelper 实例并将其附加到 RecyclerView
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        // 获取从 webViewActivity 传递的数据
        String selectedUrl = getIntent().getStringExtra("selectedUrl");
        if (selectedUrl != null) {
            adapter.highlightSelectedPage(selectedUrl);
            //highlightSelectedPage(selectedUrl);
        }

        newPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行跳转到 MainActivity 的操作
                Intent mainActivityIntent = new Intent(treeActivity.this, MainActivity.class);
                Log.d(TAG, "add link");
                startActivity(mainActivityIntent);
            }
        });

    }
    @Override
    public void onImageClicked(WebPage webPage) {
        Log.d(TAG, "linster");
        // 在这里处理图片的点击事件，执行展开和收缩操作
        int position = webPages.indexOf(webPage);
        toggleChildren(webPage);
        //toggleItem(position);
    //collapseAllChildren(webPage);
    }

    private List<WebPage> getWebPagesFromDatabase() {
        return databaseHelper.getAllWebPages();
        //return databaseHelper.getChildWebPages(-1);

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

//选中页面的方法

    // 判断页面是否有子页面
    // 判断页面是否有子页面
    private boolean webPageHasChildren(WebPage webPage) {
        return databaseHelper.webPageHasChildren(webPage);
    }


    // 展开或收缩子页
    private void toggleChildren(WebPage webPage) {
        adapter.toggleChildren(webPage);
    }
 /*   private void toggleItem(int position) {
        adapter.toggleChildren(position);
    }*/

    // 打开页面
    private void openWebPage(WebPage webPage) {
        Intent intent = new Intent(this, webViewActivity.class);
        intent.putExtra("message","url");
        intent.putExtra("url", webPage.getUrl());
        boolean save=false;
        intent.putExtra("save", save);
        startActivity(intent);

    }

    // 根据传递的选中页面URL，找到对应项并显示点击效果
    private void highlightSelectedPage(String selectedUrl) {
        webPages1 = getWebPagesFromDatabase();
        for (int i = 0; i < webPages1.size(); i++) {
            WebPage webPage = webPages1.get(i);


            if (webPage.getUrl().equals(selectedUrl)) {
                adapter.setSelectedPosition(i);
                adapter.notifyDataSetChanged();
                PhoneConnectionService.sendWebPageDataToWatch(webPage,true);
                // 滚动到选中的位置
                recyclerView.scrollToPosition(i);
                break;
            }
        }
    }

    /*private void highlightSelectedPage(String selectedUrl) {
        webPages1 = getWebPagesFromDatabase();
        for (int i = 0; i < webPages1.size(); i++) {
            WebPage webPage = webPages1.get(i);

            if (webPage.getUrl().equals(selectedUrl)) {
                adapter.setSelectedPosition(i);
                adapter.notifyDataSetChanged();
                PhoneConnectionService.sendWebPageDataToWatch(webPage, true);

                // 滚动到选中的位置
                recyclerView.smoothScrollToPosition(i);

                // 使用RecyclerView的OnScrollListener来监听滚动完成
                int finalI = i;
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            // 获取滚动完成后的View并设置点击效果
                            View view = recyclerView.getLayoutManager().findViewByPosition(finalI);
                            if (view != null) {
                                view.setActivated(true);
                            }

                            // 及时移除监听器，以免重复触发
                            recyclerView.removeOnScrollListener(this);
                        }
                    }
                });
                break;
            }
        }
    }*/
/*
    private void highlightSelectedPage(String selectedUrl) {
        webPages1 = getWebPagesFromDatabase();
        for (int i = 0; i < webPages1.size(); i++) {
            WebPage webPage = webPages1.get(i);

            if (webPage.getUrl().equals(selectedUrl)) {
                adapter.setSelectedPosition(i);
                Log.d(TAG, "highlightSelectedPage: "+i);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });

                PhoneConnectionService.sendWebPageDataToWatch(webPage, true);

                // 滚动到选中的位置
                recyclerView.smoothScrollToPosition(i);

                // 在滚动完成后手动设置点击效果
                int finalI = i;
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            // 获取滚动完成后的View并设置点击效果
                            View view = recyclerView.getLayoutManager().findViewByPosition(finalI);
                            if (view != null) {
                                view.setActivated(true);
                            }
                            // 移除监听器，以免重复触发
                            recyclerView.removeOnScrollListener(this);
                        }
                    }
                });

                break;
            }
        }
    }
*/



    @Override
    protected void onDestroy() {
        // Unregister the BroadcastReceiver when the activity is destroyed
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(confirmationReceiver);
        super.onDestroy();
    }
}