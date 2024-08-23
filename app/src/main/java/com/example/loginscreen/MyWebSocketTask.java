package com.example.loginscreen;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MyWebSocketTask extends AsyncTask<Void, Void, Void> {

    private WebSocketCallback callback;

    private String username;
    private String password;

    public MyWebSocketTask(WebSocketCallback callback, String username, String password) {
        this.callback = callback;
        this.username = username;
        this.password = password;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String url = "https://uiot.ixxc.dev/auth/realms/master/protocol/openid-connect/token";
        String data = "client_id=openremote&username=" + username + "&password=" + password +"&grant_type=password";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(RequestBody.create(data, MediaType.parse("application/x-www-form-urlencoded")))
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            String accessToken = parseAccessToken(responseBody);
            connectToWebSocket(accessToken);
        } catch (IOException | JSONException e) {
            handleError(e.getMessage());
        }

        if (callback != null) {
            callback.onWebSocketTaskComplete();
        }

        return null;
    }

    private String parseAccessToken(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        return json.getString("access_token");
    }

    private void connectToWebSocket(String accessToken) {
        String realm = "master";
        String websocketUrl = "wss://uiot.ixxc.dev/websocket/events?Realm=" + realm + "&Authorization=Bearer%20" + accessToken;

        OkHttpClient client = new OkHttpClient();
        Request websocketRequest = new Request.Builder().url(websocketUrl).build();

        WebSocket webSocket = client.newWebSocket(websocketRequest, new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                if (callback != null) {
                    callback.onWebSocketMessage(text);
                }

                Log.d("WebSocket", "Response:" + text);
            }

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                if (callback != null) {
                    callback.onWebSocketConnected();
                }
                String message = "REQUESTRESPONSE:{\"messageId\":\"read-assets:5zI6XqkQVSfdgOrZ1MyWEf:AssetEvent1\",\"event\":{\"eventType\":\"read-assets\",\"assetQuery\":{\"ids\":[\"5zI6XqkQVSfdgOrZ1MyWEf\"]}}}";
                try {
                    sendMessage(webSocket, message);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                if (callback != null) {
                    callback.onWebSocketClosed();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                if (callback != null) {
                    callback.onWebSocketError(t.getMessage());
                }
            }
        });
    }

    private void sendMessage(WebSocket webSocket, String message) throws JSONException {
        webSocket.send(message);
        Log.d("WebSocket", "Sent JSON message: " + message);
    }

    private void handleError(String errorMessage) {
        if (callback != null) {
            callback.onWebSocketError(errorMessage);
        }
        Log.e("WebSocket", "Error: " + errorMessage);
    }
}

