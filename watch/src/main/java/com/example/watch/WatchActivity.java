package com.example.watch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import fragment.TabFragment;

public class WatchActivity extends AppCompatActivity {

    private static final String TAG = "WatchActivity";
    private static final int PHONE_PORT = 8000; // 手机监听的端口号

    private WatchConnectionService watchConnectionService; // 保存WatchConnectionService的引用
    private TabFragment tabFragment; // 添加对 TabFragment 的引用


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
    }

    public void startServiceAndOpenFragment(View view) {
        // 启动 WatchConnectionService
        startConnectionService();

        // 打开 TabFragment
        openTabFragment();
    }

    public void startConnectionService() {
        Intent intent = new Intent(this, WatchConnectionService.class);
        startService(intent);
    }

    private void openTabFragment() {
        if (tabFragment == null) {
            tabFragment = new TabFragment();
            tabFragment.setWatchConnectionService(watchConnectionService);
            // 隐藏按钮
            findViewById(R.id.startServiceButton).setVisibility(View.GONE);
            watchConnectionService.setTabFragment(tabFragment); // 设置TabFragment的引用

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.TabFragment, tabFragment); // 用合适的容器 ID
            fragmentTransaction.commit();
        }
    }

    // 在onResume()方法中绑定WatchConnectionService
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, WatchConnectionService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // 在onPause()方法中解绑WatchConnectionService
    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WatchConnectionService.WatchConnectionServiceBinder binder = (WatchConnectionService.WatchConnectionServiceBinder) service;
            watchConnectionService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 在这里处理WatchConnectionService意外断开连接的情况
            // 可以进行一些重连操作
        }
    };
/*    private void setupTabFragment() {
        Log.d(TAG, "setupTabFragment called");
        if (tabFragment == null) {
            tabFragment = new TabFragment();
            tabFragment.setWatchConnectionService(watchConnectionService);
            watchConnectionService.setTabFragment(tabFragment); // 设置TabFragment的引用

            *//*FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.TabFragment, tabFragment);
            fragmentTransaction.commit();*//*
        }}*/

    public WatchConnectionService getWatchConnectionService() {
        return watchConnectionService;
    }

    // 其他代码...
}
