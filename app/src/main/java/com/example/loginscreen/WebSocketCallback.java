package com.example.loginscreen;

public interface WebSocketCallback {
    void onWebSocketTaskComplete();
    void onWebSocketConnected();
    void onWebSocketMessage(String message);
    void onWebSocketClosed();
    void onWebSocketError(String errorMessage);
}
