package com.example.loginscreen;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

public class MapView extends Fragment implements WebSocketCallback {


    public MapView() {

    }

    org.osmdroid.views.MapView mapView;
    Marker startMarker;

    GeoPoint UITPoint = new GeoPoint(10.87010241486628, 106.80306583314845);
    GeoPoint EPoint = new GeoPoint(10.869795551725913, 106.80258404109576);

    String getValue(JsonObject assets, String metric, String element){
        return assets
                .getAsJsonObject("attributes")
                .getAsJsonObject(metric)
                .get(element)
                .getAsString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", getActivity().MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        String password = sharedPreferences.getString("password", null);
        String savedAccessToken = sharedPreferences.getString("access_token", null);

        MyWebSocketTask webSocketTask = new MyWebSocketTask(this, username, password);
        webSocketTask.execute();

        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();

            // Đặt tiêu đề cho ActionBar
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle("Map");
            }
        }

        View view = inflater.inflate(R.layout.map, container, false);

        Configuration.getInstance().load(view.getContext(),
                getActivity().getSharedPreferences("osmdroid", getContext().MODE_PRIVATE));

        mapView = view.findViewById(R.id.mapView);

        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);






        mapView.getController().setZoom(20);
        mapView.getController().setCenter(UITPoint);

        mapView.setMinZoomLevel(5.0);
        mapView.setMaxZoomLevel(19.8);


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

        Log.d("Check", message);

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

                String assetName = assets.get("name").getAsString();
                if (assets.has("attributes") && assets.getAsJsonObject("attributes").has("humidity")) {
                    String humidityValue = getValue(assets, "humidity", "value");
                    String temperatureValue = getValue(assets, "temperature", "value");
                    String rainfallValue = getValue(assets, "rainfall", "value");
                    String windSpeed = getValue(assets, "windSpeed", "value");

                    String notificationMessage = "Humidity value: " + humidityValue +
                            "\nTemperature value: " + temperatureValue +
                            "\nRainfall value: " + rainfallValue +
                            "\nWind Speed: " + windSpeed;

                    Drawable customMarker = getResources().getDrawable(R.mipmap.asset_foreground);
                    customMarker.setBounds(0, 0, customMarker.getIntrinsicWidth(), customMarker.getIntrinsicHeight());

                    startMarker = new Marker(mapView);
                    startMarker.setPosition(EPoint);
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    startMarker.setIcon(customMarker);

                    startMarker.setTitle("Asset: " + assetName);
                    startMarker.setSnippet(notificationMessage);

                    startMarker.setInfoWindow(new CustomInfoWindow(mapView));
                    startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, org.osmdroid.views.MapView mapView) {
                            if (marker.isInfoWindowShown()) {
                                marker.closeInfoWindow();
                            } else {
                                marker.showInfoWindow();
                            }
                            return true;
                        }
                    });

                    mapView.getOverlays().add(startMarker);

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