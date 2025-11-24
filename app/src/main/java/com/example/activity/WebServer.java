package com.example.activity;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class WebServer extends WebSocketServer {
    private static final int SERVER_PORT = 8080;

    public WebServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // 连接建立时的处理，可以发送欢迎消息等
        Log.d("start","connected");
        conn.send("Welcome! You are connected.");
        Log.d("start","connected");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // 连接关闭时的处理
        Log.d("start","closed");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // 收到消息时的处理
        // 在这里可以处理手机端收到的消息，并向手表端发送消息
        conn.send("Hello from server!");
        Log.d("start","received");
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        // 发生错误时的处理
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        // WebSocket服务器启动时的处理
        Log.d("start","started");
    }
}
