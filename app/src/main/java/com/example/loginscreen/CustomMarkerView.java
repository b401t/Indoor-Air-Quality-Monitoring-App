package com.example.loginscreen;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class CustomMarkerView extends MarkerView {

    private final TextView humidityTextView;
    private final TextView dateTimeTextView;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        humidityTextView = findViewById(R.id.humidityTextView);
        dateTimeTextView = findViewById(R.id.dateTimeTextView);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        humidityTextView.setText("Humidity: " + e.getY());

        // Assuming your X value is a timestamp in seconds
        long timestamp = (long) e.getX() * 1000;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(timestamp));
        dateTimeTextView.setText("Date Time: " + formattedDate);

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }
}