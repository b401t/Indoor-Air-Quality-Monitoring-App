package com.example.loginscreen;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeekAxisValueFormatter extends ValueFormatter {

    private final List<Long> timestamps;

    public WeekAxisValueFormatter(List<Long> timestamps) {
        this.timestamps = timestamps;
    }

    @Override
    public String getFormattedValue(float value) {
        long timestampMillis = (long) value * 1000;
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
        Date date = new Date(timestampMillis);
        return dateFormat.format(date);
    }
}
