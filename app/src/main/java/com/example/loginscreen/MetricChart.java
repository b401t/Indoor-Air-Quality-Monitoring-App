package com.example.loginscreen;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MetricChart extends AppCompatActivity {

    String[] items2 = {"Day", "Week", "Month"};

    private long fromTimestamp;
    private long toTimestamp;

    AutoCompleteTextView autoCompleteTXT2;

    ArrayAdapter<String> adapterItems2;

    Button showButton;

    String choose;


    private LineChart lineChart;
    private int colorMagentaDark = Color.argb(255, 139, 0, 139);

    private String attribute;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.metric_chart);

        attribute = getIntent().getStringExtra("attribute");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        autoCompleteTXT2 = findViewById(R.id.auto_complete_txt2);

        adapterItems2 = new ArrayAdapter<>(this, R.layout.list_item, items2);

        autoCompleteTXT2.setAdapter(adapterItems2);


        autoCompleteTXT2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();

                LocalDateTime now = LocalDateTime.now();
                toTimestamp = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                switch (item) {
                    case "Day":
                        fromTimestamp = now.minusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        choose = "Day";
                        break;
                    case "Week":
                        fromTimestamp = now.minusWeeks(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        choose = "Week";
                        break;
                    case "Month":
                        choose = "Month";
                        fromTimestamp = now.minusMonths(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        break;
                }
            }
        });

        CustomMarkerView mv = new CustomMarkerView(this, R.layout.info_layout);

        showButton = (Button)findViewById(R.id.show);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lineChart = findViewById(R.id.lineChart);
                lineChart.getDescription().setText(attribute);
                lineChart.setPinchZoom(false);
                lineChart.setScaleEnabled(false);
                lineChart.setDoubleTapToZoomEnabled(false);

                fetchDataAndDisplayChart();

                lineChart.setMarker(mv);
            }
        });
    }

    private void fetchDataAndDisplayChart() {

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String access_token = sharedPreferences.getString("access_token", null);

        String url = "https://uiot.ixxc.dev/api/master/asset/datapoint/5zI6XqkQVSfdgOrZ1MyWEf/attribute/" + attribute;
        String token = "Bearer " + access_token;
        String payload = "{\"type\":\"lttb\",\"fromTimestamp\":" + fromTimestamp + ",\"toTimestamp\":" + toTimestamp + ",\"amountOfPoints\":100}";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", token)
                .post(RequestBody.create(MediaType.parse("application/json"), payload))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d("MainActivity", "Response received: " + jsonResponse);
                        JSONArray jsonArray = new JSONArray(jsonResponse);
                        final List<Entry> entries = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            long x = jsonObject.getLong("x");
                            double y = jsonObject.getDouble("y");
                            entries.add(new Entry((float) x / 1000, (float) y));
                            Log.d("MainActivity", "Entry added: x=" + x + ", y=" + y);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LineDataSet lineDataSet = new LineDataSet(entries, attribute);
                                lineDataSet.setColor(colorMagentaDark);
                                lineDataSet.setFillColor(colorMagentaDark);
                                lineDataSet.setDrawValues(false);lineDataSet.setDrawValues(false);
                                lineDataSet.setDrawFilled(true);
                                lineDataSet.setLineWidth(1.5f);

                                LineData lineData = new LineData(lineDataSet);
                                lineChart.setData(lineData);
                                lineChart.invalidate();

                                List<Long> timestamps = new ArrayList<>();

                                for (Entry entry : entries) {
                                    timestamps.add((long) entry.getX() * 1000);
                                }

                                XAxis xAxis = lineChart.getXAxis();

                                switch (choose) {
                                    case "Day":
                                        xAxis.setValueFormatter(new DayAxisValueFormatter(timestamps));
                                        break;
                                    case "Week":
                                        xAxis.setValueFormatter(new WeekAxisValueFormatter(timestamps));
                                        break;
                                    case "Month":
                                        xAxis.setValueFormatter(new MonthAxisValueFormatter(timestamps));
                                        break;
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
