package com.example.activity;

import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import helper.DatabaseHelper;
import helper.WebPage;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.activity.CaptureScreeUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class webViewActivity extends AppCompatActivity {

    private WebView webView;
    private List<WebPage> webPages;
    private BottomNavigationView bottomNavigationView;

    // 适配手表的缩略图尺寸

    private byte[] currentThumbnail; // 用于保存当前缩略图数据
    private long currentParentId = -1; // 默认为-1，表示顶级页面
    private long previousPageId = -1; // 用于保存前一个页面的ID
    private long existingParentID = -1;
    private long currentPageId = -1; // 初始化为无效的值
private boolean save1=false;
    private String currentUrl; // 用于保存前一个页面的url
    public WebPage webPage;
    //private ArrayList webpages;
    private DatabaseHelper databaseHelper;
    //private ImageView thumbnailImageView;
    public boolean save=true;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private FloatingActionButton treeButton;
    private BroadcastReceiver startWebViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ");
            String url = intent.getStringExtra("url");
            WebSettings webSettings = webView.getSettings();
            webSettings.setSupportZoom(true); // 允许缩放
            webSettings.setBuiltInZoomControls(true); // 显示缩放控件

            Intent webViewIntent = new Intent(webViewActivity.this, webViewActivity.class);
            webViewIntent.putExtra("url", url);
            webViewIntent.putExtra("save", save); // 将save的值传递给webViewActivity
            Log.d(TAG, "onReceive1: "+save);
            startActivity(webViewIntent);
        }
    };
    private BroadcastReceiver zoomWebViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type"); // 使用 intent 获取数据
            Log.d(TAG, "onReceive: "+type);
            if ("zoomin".equals(type)) { // 注意这里使用 equals 方法来比较字符串
                performZoomIn();
            }
            if ("zoomout".equals(type)) {
                performZoomOut();
            }
            if ("zoom".equals(type)) {
                showConnectedToast();
            }
        }
    };
    private void showConnectedToast() {
        // 在UI线程中显示Toast消息
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(webViewActivity.this, "zoom mode", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Log.d(TAG, "onCreate web view : ");
        // 注册广播接收器
        // 注册广播接收器
        IntentFilter zoomWebViewFilter = new IntentFilter("zoom-webview-action");
        LocalBroadcastManager.getInstance(this).registerReceiver(zoomWebViewReceiver, zoomWebViewFilter);
        IntentFilter startWebViewFilter = new IntentFilter("start-webview-action");
        LocalBroadcastManager.getInstance(this).registerReceiver(startWebViewReceiver, startWebViewFilter);
        // 设置支持缩放
/*
        WebSettings webSettings = webView.getSettings();
        webSettings.setSupportZoom(true);
        // 设置使用内置缩放机制
        webSettings.setBuiltInZoomControls(true);
        // 设置显示缩放控制按钮
        webSettings.setDisplayZoomControls(true);
*/



        long parentId = getIntent().getLongExtra("parentId", 0);
        currentParentId = parentId;
        treeButton=findViewById(R.id.fabTree);
        treeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行跳转到 ClientActivity 的操作
                Intent treeIntent = new Intent(webViewActivity.this, treeActivity.class);
                startActivity(treeIntent);
            }
        });


        // 实例化DatabaseHelper对象
        databaseHelper = new DatabaseHelper(this);

        webView = findViewById(R.id.web_view);

        webPages = new ArrayList<>();

        webView.getSettings().setJavaScriptEnabled(true); // 确保启用了 JavaScript
// 在适当的位置使用

        boolean isFromHomePage = getIntent().getBooleanExtra("fromHomePage", false);
        if (isFromHomePage) {
            currentParentId = -1; // 从主页进入的页面，设置 parent id 为 -1
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                long previousPageId = databaseHelper.getPageIdByUrl(url);

                if (url != null&& save==true) {
                    //Log.d("TAG", " pre " + previousPageId + " cur " + currentParentId + " par " + parentId);
                    if (databaseHelper.Existed(url) ) {
                        Log.d("TAG", "WebView URL: " + url);
                        Log.d("TAG", "repeated: " + url + previousPageId);
                        Intent intent = new Intent(webViewActivity.this, treeActivity.class);
                        intent.putExtra("selectedUrl", url);
                        startActivity(intent);
                        finish(); // 结束当前活动，以便返回时回到 treeActivity// 不进行页面加载
                    } else {
                        Log.d("TAG", "not existed: " + url+", "+save);
                        return false;// 进行页面加载
                    }
                }
                //webView.loadUrl(url);
                return false;// 继续 WebView 自行加载 URL
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                currentUrl=url;
                super.onPageFinished(view, url);
                // 获取网页的真实标题
                String pageTitle = view.getTitle();
                if (save==true) {
                    // 等待页面渲染完成后再进行截图保存操作
                    webView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            saveWebPageToDatabase(webView, url, pageTitle, currentParentId);
                            // 进行截图保存操作
                        }
                    }, 500); // 可根据实际情况调整延迟时间
                }else {
                    save = true;
                    Log.d("TAG", "not saved: " + url);
                }

            }
        });

        // 加载网页
        String url = getIntent().getStringExtra("url");
        Log.d(TAG, "received url:"+url);
        save=getIntent().getBooleanExtra("save",true);
        Log.d(TAG, "save: "+save);
        //Log.d(TAG, "onReceive2: "+save);
        webView.loadUrl(url); // 在这里加载网页
    }

    // 保存网页到数据库的方法
// 获取 WebView 的内容高度
    private static final float MM_TO_INCH = 0.0393701f; // 1毫米 = 0.0393701英寸
    private static final float DESIRED_ASPECT_RATIO = 170.0f / 190.0f; // 期望的宽高比例

    private int convertMmToPx(float mm) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(mm * displayMetrics.densityDpi * MM_TO_INCH);
    }

       private void saveWebPageToDatabase(WebView view, String url, String pageTitle, long previousPageId) {
        if(save==false){
            save=true;
            return;
        }
        float THUMBNAIL_WIDTH=webView.getWidth();
           int THUMBNAIL_WIDTH_PX = convertMmToPx(THUMBNAIL_WIDTH);

           // 计算所需的缩略图高度，以适应期望的宽高比例
           int THUMBNAIL_HEIGHT_PX = Math.round(THUMBNAIL_WIDTH_PX * 190 / 170);

           // 使用CaptureScreeUtil截取WebView内容并创建缩略图
           Bitmap capturedBitmap = CaptureScreeUtil.capture(view.getWidth(), view.getWidth()*15/17, view);


           // Convert the cropped Bitmap to a byte array for storing in the WebPage object
           ByteArrayOutputStream stream = new ByteArrayOutputStream();
           capturedBitmap.compress(Bitmap.CompressFormat.JPEG , 50, stream);
           currentThumbnail = stream.toByteArray();

           WebPage webPage = new WebPage(pageTitle, url, new Date());
           webPage.setThumbnail(currentThumbnail);
           //updateUI(webPage);
           Log.d(TAG, "saveWebPageToDatabase: "+currentThumbnail.length);
           Log.d("TAG", "saveWebPageToDatabase: " + url + ":  " + pageTitle+"size:"+currentThumbnail.length);
           DatabaseHelper databaseHelper = new DatabaseHelper(webViewActivity.this);
           //long previousPageId = databaseHelper.getParentIdByUrl(url);
           long parentId =currentPageId;
           // 保存网页数据到数据库
           long id = databaseHelper.insertWebPage(webPage, parentId);
           Log.d("TAG", "send " + id + "," + webPage.getTitle() + " id：" + parentId);
           currentPageId=id;
           // 更新 currentParentId 以便下次保存新的根页面
           if (parentId == -1) {
               currentParentId = id;

           }

           Log.d("TAG", "更新 " + currentParentId);

           // 恢复 WebView 的滚动位置
           view.scrollTo(0, 0);
       }



    // 在 TreeActivity 返回时更新当前父页面ID
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            long parentId = data.getLongExtra("parentId", -1);
            if (parentId != -1) {
                // 根据 parentId 查找对应的 WebPage 对象
                for (WebPage page : webPages) {
                    if (page.getId() == parentId) {
                        currentParentId = parentId;
                        break;
                    }
                }
            }
        }
    }


    // 在 webViewActivity 中的某个方法中执行放大操作
    private void performZoomIn() {
        WebSettings webSettings = webView.getSettings();
        float currentTextZoom = webSettings.getTextZoom(); // 获取当前文本缩放级别
        int newTextZoom = (int) (currentTextZoom + 35); // 增加文本缩放级别，可以根据需要调整增量

        // 设置新的文本缩放级别
        webSettings.setTextZoom(newTextZoom);
    }

    private void performZoomOut() {
        WebSettings webSettings = webView.getSettings();
        float currentTextZoom = webSettings.getTextZoom(); // 获取当前文本缩放级别
        int newTextZoom = (int) (currentTextZoom - 35); // 减小文本缩放级别，可以根据需要调整增量

        // 设置新的文本缩放级别
        webSettings.setTextZoom(newTextZoom);
    }


    @Override
    public void onBackPressed() {
        // 在WebView中返回时，如果可以返回上一页则执行返回操作，否则关闭WebViewActivity
        if (webView.canGoBack()) {
            save=false;
            currentPageId=databaseHelper.getParentIdByUrl(currentUrl);
            webView.goBack();
            //super.onBackPressed();

        } else {
            long par=databaseHelper.getParentIdByUrl(currentUrl);
            Log.d(TAG, "par: "+par);
            Intent intent = new Intent(this, treeActivity.class);
            startActivity(intent);
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        // 取消广播接收器的注册
        LocalBroadcastManager.getInstance(this).unregisterReceiver(startWebViewReceiver);
        super.onDestroy();
    }

}
