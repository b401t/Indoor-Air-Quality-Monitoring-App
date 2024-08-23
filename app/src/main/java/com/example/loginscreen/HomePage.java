package com.example.loginscreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomePage extends AppCompatActivity {

    private String urlUser = "https://uiot.ixxc.dev/api/master/user/user";

    private OkHttpClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = new OkHttpClient.Builder()
                .cookieJar(new MyCookieJar())
                .build();

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String savedAccessToken = sharedPreferences.getString("access_token", null);
        Request userRequest = new Request.Builder()
                .url(urlUser)
                .header("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + savedAccessToken)
                .build();

        client.newCall(userRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful())
                {
                    Log.d("UserInfo", response.body().string());
                    Intent intent = new Intent(HomePage.this, DashBoard.class);
                    startActivity(intent);
                    finish();
                }
            }
        });


        setContentView(R.layout.activity_home_page_screen);

        TextView loginhomepage = findViewById(R.id.loginhomepage);
        TextView signuphomepage = findViewById(R.id.signuphomepage);

        loginhomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Login.class);
                startActivity(intent);
            }
        });

        signuphomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, SignUp.class);
                startActivity(intent);
            }
        });
    }
}