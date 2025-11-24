package com.example.activity;

//import static com.example.common.ServerHelper.EXTRA_SERVER_IP;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.os.Bundle;

//import com.example.watch.WatchActivity;


public class ClientActivity extends AppCompatActivity {

    private static final String TAG = "ClientActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_client);

        // 在这里可以向手表发送数据
        startConnectionService();
    }

    private void startConnectionService() {
        Intent intent = new Intent(this, PhoneConnectionService.class);
        startService(intent);
    }

    private void sendConfirmationToTreeActivity() {
        Intent intent = new Intent("confirmation-action");
        intent.putExtra("message", "CONFIRMED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // 注意：不需要在这里关闭套接字和流，由连接服务负责管理连接和通信
}
