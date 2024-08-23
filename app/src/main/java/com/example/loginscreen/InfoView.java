package com.example.loginscreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InfoView extends Fragment {

    public InfoView() {
        // Required empty public constructor
    }

    Button logout_button;

    TextView username, firstname, lastname, email;

    private String urlUser = "https://uiot.ixxc.dev/api/master/user/user";
    private String urlAccount = "https://uiot.ixxc.dev/auth/realms/master/account/";

    private OkHttpClient client;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();

            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle("Account");
            }
        }
        View view = inflater.inflate(R.layout.info, container, false);

        username = (TextView)view.findViewById(R.id.username);
        firstname = (TextView)view.findViewById(R.id.firstName);
        lastname = (TextView)view.findViewById(R.id.lastName);
        email = (TextView)view.findViewById(R.id.email);


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", getActivity().MODE_PRIVATE);
        String savedAccessToken = sharedPreferences.getString("access_token", null);

        client = new OkHttpClient.Builder()
                .cookieJar(new MyCookieJar())
                .build();

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
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("BaoPc", responseData);
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        String firstNameValue = jsonObject.getString("firstName");
                        String lastNameValue = jsonObject.getString("lastName");
                        String usernameValue = jsonObject.getString("username");
                        String emailValue = jsonObject.getString("email");

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                username.setText(usernameValue);
                                firstname.setText(firstNameValue);
                                lastname.setText(lastNameValue);
                                email.setText(emailValue);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        logout_button = view.findViewById(R.id.logout_button);

        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CookieJar cookieJar = client.cookieJar();
                if (cookieJar instanceof MyCookieJar) {
                    ((MyCookieJar) cookieJar).clearAllCookies();
                }

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();

                Intent intent = new Intent(getActivity(), HomePage.class);
                startActivity(intent);

                Intent serviceIntent = new Intent(getActivity(), WebSocketService.class);
                getActivity().stopService(serviceIntent);

                Intent refreshTokenIntent = new Intent(getActivity(), RefreshTokenService.class);
                getActivity().stopService(refreshTokenIntent);
                getActivity().finish();

            }
        });


        return view;
    }
}