package com.example.common;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import com.example.common.ServerIpCallback;

public class ServerHelper {

    private static final int SERVER_PORT = 8000; // Add this line to define the server port
    private static WebSocketClient client;
    public static final String EXTRA_SERVER_IP = "extra_server_ip";
    private static Context appContext; // 添加一个静态变量用于保存全局的Context

    // 添加一个静态方法来设置全局的Context
    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }
    public static void connectToServer(String serverIP, ServerIpCallback callback) {
        try {
            // 使用获取到的IP地址连接WebSocket服务器
            String serverUrl = "ws://" + serverIP + ":" + SERVER_PORT;
            client = new WebSocketClient(new URI(serverUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d("acti", serverIP);
                    // 连接建立时的处理
                    // 可以在此处执行其他初始化操作
                    Log.d("acti", "WebSocket connection opened");
                    callback.onServerIpReceived(serverIP); // 回调传递服务器IP
                }

                @Override
                public void onMessage(String message) {
                    // 收到消息时的处理
                    // 在这里处理从服务器接收到的消息
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    // 连接关闭时的处理
                    // 可以在此处执行一些清理操作
                }

                @Override
                public void onError(Exception ex) {
                    // 发生错误时的处理
                    // 可以在此处处理连接出现的异常
                    ex.printStackTrace();
                }
            };
            client.connect(); // 开始连接到WebSocket服务器
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void sendWebSocketMessage(String message) {
        if (client != null && client.isOpen()) {
            // 发送消息到服务器
            client.send(message);
        }
    }

    public static void sendIPBroadcast(String serverIP) {
        Intent intent = new Intent( "com.example.GLOBAL_SERVER_IP_BROADCAST");
        intent.putExtra(EXTRA_SERVER_IP, serverIP);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
    }
    // 可以根据需要添加其他WebSocket相关功能的实现...

    // 关闭WebSocket连接
    public static void closeWebSocketConnection() {
        if (client != null) {
            client.close();
        }
    }
}