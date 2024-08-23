package com.example.loginscreen;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RefreshTokenService extends Service {


    private static final int DELAY = 0;
    private static final int PERIOD = 24 * 60 * 60 * 1000;
    private Timer timer = new Timer();

    private OkHttpClient client;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        client = new OkHttpClient.Builder()
                .cookieJar(new MyCookieJar())
                .build();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateToken();
                Log.d("testservice", "successfull");
            }
        }, DELAY, PERIOD);

    }

    private void updateToken() {

        String urlToken = "https://uiot.ixxc.dev/auth/realms/master/protocol/openid-connect/token";

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String saveUsername = sharedPreferences.getString("username", null);
        String savePassword = sharedPreferences.getString("password", null);

        RequestBody data = new FormBody.Builder()
                .add("client_id", "openremote")
                .add("username", saveUsername)
                .add("password", savePassword)
                .add("grant_type", "password")
                .build();

        Request request = new Request.Builder()
                .url(urlToken)
                .post(data)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    JsonObject jsonObject = new Gson().fromJson(responseData, JsonObject.class);
                    String accessToken = jsonObject.get("access_token").getAsString();

                    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("access_token", accessToken);
                    editor.apply();
                }
            }
        });

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

