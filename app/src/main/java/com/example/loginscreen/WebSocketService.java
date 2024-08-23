package com.example.loginscreen;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Timer;
import java.util.TimerTask;

public class WebSocketService extends Service implements WebSocketCallback {

    private static final String CHANNEL_ID = "channelId";
    private static final int NOTIFICATION_ID = 1;

    private MyWebSocketTask webSocketTask;
    private static final int DELAY = 0;
    private static final int PERIOD = 60 * 60 * 1000;
    private Timer timer = new Timer();

    String getValue(JsonObject assets, String metric, String element){
        return assets
                .getAsJsonObject("attributes")
                .getAsJsonObject(metric)
                .get(element)
                .getAsString();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                startWebSocketTask();
            }
        }, DELAY, PERIOD);
    }

    private void startWebSocketTask() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        String password = sharedPreferences.getString("password", null);
        if (webSocketTask == null || webSocketTask.getStatus() == AsyncTask.Status.FINISHED) {
            webSocketTask = new MyWebSocketTask(this, username, password);
            webSocketTask.execute();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "WebSocket Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private Notification createNotification(String notification) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("WebSocketService")
                .setContentText(notification)
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocketTask != null) {
            webSocketTask.cancel(true);
        }
        timer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onWebSocketTaskComplete() {

    }

    @Override
    public void onWebSocketConnected() {

    }

    @Override
    public void onWebSocketMessage(String message) {


        String cleanedResponse = message.split(":", 2)[1].trim();
        try {
            JsonObject responseJson = JsonParser
                    .parseString(cleanedResponse)
                    .getAsJsonObject();

            if (responseJson.has("event") && responseJson.getAsJsonObject("event").has("assets")) {
                JsonObject assets = responseJson
                        .getAsJsonObject("event")
                        .getAsJsonArray("assets")
                        .get(0)
                        .getAsJsonObject();
                if (assets.has("attributes") && assets.getAsJsonObject("attributes").has("humidity")) {
                    String humidityValue = getValue(assets, "humidity", "value");
                    String temperatureValue = getValue(assets, "temperature", "value");
                    String rainfallValue = getValue(assets, "rainfall", "value");
                    String windSpeed = getValue(assets, "windSpeed", "value");

                    String notificationMessage = "Humidity value: " + humidityValue +
                            "  Temperature value: " + temperatureValue +
                            "\nRainfall value: " + rainfallValue +
                            "  Wind Speed: " + windSpeed;

                    startForeground(NOTIFICATION_ID, createNotification(notificationMessage));

                }
            }
        } catch (Exception e) {
            Log.d("MainActivity","Error extracting humidity value: " + e.getMessage());
        }

    }

    @Override
    public void onWebSocketClosed() {

    }

    @Override
    public void onWebSocketError(String errorMessage) {

    }
}