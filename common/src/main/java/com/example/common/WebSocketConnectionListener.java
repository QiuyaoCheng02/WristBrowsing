package com.example.common;

public interface WebSocketConnectionListener {
    void onWebSocketConnectionEstablished();
    void onWebSocketConnectionFailed(String errorMessage);
}
