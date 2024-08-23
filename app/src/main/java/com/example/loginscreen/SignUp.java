package com.example.loginscreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUp extends AppCompatActivity {

    public String extractFeature(String html, String tag, String attribute) {
        Document document = Jsoup.parse(html);
        Element foundElement = document.select(tag).first();

        if (foundElement != null) {
            String elementValue = foundElement.attr(attribute);
            return elementValue;
        } else {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);

        TextView return_login = findViewById(R.id.return_login);
        Button back_btn = findViewById(R.id.back_btn);
        Button btn_signUp = findViewById(R.id.btn_signUp);
        EditText firstName = findViewById(R.id.firstName);
        EditText lastName = findViewById(R.id.lastName);
        EditText username = findViewById(R.id.username);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        EditText confirm_password = findViewById(R.id.confirm_password);

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://uiot.ixxc.dev/auth/realms/master/protocol/openid-connect/auth?response_type=code&client_id=openremote&redirect_uri=https%3A%2F%2Fuiot.ixxc.dev%2Fswagger%2Foauth2-redirect.html";

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Call call = client.newCall(request);

                call.enqueue(new Callback() {
                    public void onResponse(Call call, Response response)
                            throws IOException {
                        String responseBody = response.body().string();
                        Headers headers = response.headers();
                        List<String> cookieValues = headers.values("Set-Cookie");
                        StringBuilder Cookie = new StringBuilder();

                        for (String cookie : cookieValues) {
                            Cookie.append(cookie).append(";");
                        }


                        if (response.code() == 200) {
                            String RegForm_URL = "https://uiot.ixxc.dev" + extractFeature(responseBody,"a", "href");

                            OkHttpClient secondClient = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url(RegForm_URL)
                                    .header("Cookie", Cookie.toString())
                                    .build();


                            Call secondCall = secondClient.newCall(request);

                            secondCall.enqueue(new Callback() {
                                public void onResponse(Call call, Response response)
                                        throws IOException {
                                    String RegURL = extractFeature(response.body().string(), "form", "action");

                                    RequestBody data = new FormBody.Builder()
                                            .add("firstName", firstName.getText().toString())
                                            .add("lastName", lastName.getText().toString())
                                            .add("username", username.getText().toString())
                                            .add("email", email.getText().toString())
                                            .add("password", password.getText().toString())
                                            .add("password-confirm", confirm_password.getText().toString())
                                            .add("register", "")
                                            .build();


                                    OkHttpClient thirdClient = new OkHttpClient();
                                    Request request = new Request.Builder()
                                            .url(RegURL)
                                            .header("Cookie", Cookie.toString())
                                            .post(data)
                                            .build();
                                    Call thirdCall = thirdClient.newCall(request);
                                    thirdCall.enqueue(new Callback() {
                                        public void onResponse(Call call, Response response)
                                                throws IOException {

                                            Document document = Jsoup.parse(response.body().string());
                                            Elements redTextElements = document.select("span.red-text");
                                            Element helperTextElement = document.select("span[data-error]").first();
                                            if (!redTextElements.isEmpty()) {
                                                Element errorElement = redTextElements.first();
                                                String errorMessage = errorElement.text();
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                            else if (helperTextElement != null) {
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), helperTextElement.attr("data-error"), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                            else {
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }

                                        public void onFailure(Call call, IOException e) {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                }

                                public void onFailure(Call call, IOException e) {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });

                        }
                    }

                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

        return_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, HomePage.class);
                startActivity(intent);
            }
        });
    }
}