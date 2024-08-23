package com.example.loginscreen;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationBarView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DashBoard extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener{
    NavigationBarView navigationBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        navigationBarView = findViewById(R.id.bottomNavigationView);
        navigationBarView.setOnItemSelectedListener(this);
        navigationBarView.setSelectedItemId(R.id.person);


        Intent serviceIntent = new Intent(this, WebSocketService.class);
        startService(serviceIntent);

        Intent refreshToken = new Intent(this , RefreshTokenService.class);
        startService(refreshToken);


    }
    Home home = new Home();
    MapView mapView = new MapView();
    InfoView infoView = new InfoView();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new Home()).commit();
            return true;
        } else if (itemId == R.id.graph) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new MapView()).commit();
            return true;
        } else if (itemId == R.id.person) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new InfoView()).commit();
            return true;
        }
        return false;
    }

}