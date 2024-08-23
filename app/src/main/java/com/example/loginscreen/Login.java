package com.example.loginscreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity {

    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        TextView to_signup = findViewById(R.id.to_signup);
        Button btn_back = findViewById(R.id.btn_back);
        Button btn_login = findViewById(R.id.btn_login);
        EditText editText_username = findViewById(R.id.username);
        EditText editText_password = findViewById(R.id.password);

        client = new OkHttpClient.Builder()
                .cookieJar(new MyCookieJar())
                .build();

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editText_username.getText().toString();
                String password = editText_password.getText().toString();

                String urlToken = "https://uiot.ixxc.dev/auth/realms/master/protocol/openid-connect/token";
                String urlUser = "https://uiot.ixxc.dev/api/master/user/user";

                RequestBody data = new FormBody.Builder()
                        .add("client_id", "openremote")
                        .add("username", username)
                        .add("password", password)
                        .add("grant_type", "password")
                        .build();

                Request request = new Request.Builder()
                        .url(urlToken)
                        .post(data)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    public void onResponse(Call call, Response response)
                            throws IOException {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            JsonObject jsonObject = new Gson().fromJson(responseData, JsonObject.class);
                            String accessToken = jsonObject.get("access_token").getAsString();

                            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("access_token", accessToken);
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.apply();

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
                                    if(response.isSuccessful()) {
                                        Intent intent = new Intent(Login.this, DashBoard.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Login.this, "Invalid user credentials", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }

                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> Toast.makeText(Login.this, e.toString(), Toast.LENGTH_SHORT).show());
                    }
                });

                SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

            }
        });

        to_signup.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SignUp.class);
            startActivity(intent);
        });

        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, HomePage.class);
            startActivity(intent);
        });
    }
}