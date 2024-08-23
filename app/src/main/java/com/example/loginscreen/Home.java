package com.example.loginscreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Home extends Fragment implements WebSocketCallback {

    public Home() {

    }

    TextView humidity_tv, temperature_tv, rainfall_tv, windSpeed_tv;
    TextView dashboardName;
    TextView days;

    TextView place;

    String getValue(JsonObject assets, String metric, String element){
        return assets
                .getAsJsonObject("attributes")
                .getAsJsonObject(metric)
                .get(element)
                .getAsString();
    }

    CardView rainfall;
    CardView humidity;
    CardView temperature;
    CardView windspeed;

    private String urlUser = "https://uiot.ixxc.dev/api/master/user/user";

    private OkHttpClient client;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_page, container, false);

        rainfall = view.findViewById(R.id.rainfall);
        humidity = view.findViewById(R.id.humidity);
        temperature = view.findViewById(R.id.temperature);
        windspeed = view.findViewById(R.id.windSpeed);

        humidity_tv = (TextView)view.findViewById(R.id.humidity_tv);
        rainfall_tv = (TextView)view.findViewById(R.id.rainfall_tv);
        temperature_tv = (TextView)view.findViewById(R.id.temperature_tv);
        windSpeed_tv = (TextView)view.findViewById(R.id.windSpeed_tv);

        dashboardName = (TextView)view.findViewById(R.id.dashboardName);
        place = (TextView)view.findViewById(R.id.place);

        days = (TextView)view.findViewById(R.id.days);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d, MMMM, yyyy");
        String currentDateAndTime = dateFormat.format(calendar.getTime());

        days.setText(currentDateAndTime);


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", getActivity().MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        String password = sharedPreferences.getString("password", null);
        String savedAccessToken = sharedPreferences.getString("access_token", null);

        MyWebSocketTask webSocketTask = new MyWebSocketTask(this, username, password);
        webSocketTask.execute();

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
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        String firstName = jsonObject.getString("firstName");
                        String lastName = jsonObject.getString("lastName");

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dashboardName.setText("Hi, " + firstName + " " + lastName);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        rainfall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), MetricChart.class);
                intent.putExtra("attribute", "rainfall");
                startActivity(intent);
            }
        });

        humidity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), MetricChart.class);
                intent.putExtra("attribute", "humidity");
                startActivity(intent);
            }
        });

        temperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), MetricChart.class);
                intent.putExtra("attribute", "temperature");
                startActivity(intent);
            }
        });

        windspeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), MetricChart.class);
                intent.putExtra("attribute", "windSpeed");
                startActivity(intent);
            }
        });


        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();

            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle("Home");
            }
        }
        return view;
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
                    String windSpeedValue = getValue(assets, "windSpeed", "value");
                    String placeValue = getValue(assets, "place", "value");

                    humidity_tv.setText(humidityValue);
                    temperature_tv.setText(temperatureValue);
                    rainfall_tv.setText(rainfallValue);
                    windSpeed_tv.setText(windSpeedValue);
                    place.setText(placeValue);
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