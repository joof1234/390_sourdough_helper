package com.example.mainactivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SensorFragment extends Fragment {
    private static final String ARG_MAC = "device_mac";
    private static final String ARG_FIELD = "field_name";
    private static final String ARG_UNIT = "field_unit";
    private LineChart chart;
    private DatabaseReference dbRef;

    public static SensorFragment newInstance(String deviceMac, String fieldName, String unit) {
        SensorFragment fragment = new SensorFragment();
        //make a bundle to get the data across functions
        //called when we are making a new chart
        Bundle args = new Bundle();
        args.putString(ARG_MAC, deviceMac);
        args.putString(ARG_FIELD, fieldName);
        args.putString(ARG_UNIT, unit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);
        chart = view.findViewById(R.id.chart);

        //this class is based on making the chart. this is for each sensor.
        setupChart();
        return view;
    }

    private void setupChart() {
        Bundle args = getArguments();
        String deviceMac = args.getString(ARG_MAC);
        String fieldName = args.getString(ARG_FIELD);
        String unit = args.getString(ARG_UNIT);

        //TODO: MAKE SURE YOU CAN CYCLE PER DAY, AND DECTECT THE DAY YOU ARE REPRESENTING ON THE CHARTS.
        // DRILLDOWN REQUIRED>
        //get the database
        dbRef = FirebaseDatabase.getInstance().getReference("sensors/" + deviceMac + "/day_1");
        //when a new value is added to the database, to that specific device,
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Entry> entries = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    //the values may vary so here we can manage it.
                    Object value = child.child(fieldName).getValue();
                    Long timestamp = child.child("timestamp").getValue(Long.class);

                    //we want our chart to only display float values.
                    //not all values are floats, we must cast it appropriately.
                    //we can make a class later if we want to condense this.
                    if (value != null && timestamp != null) {
                        float floatValue = 0;
                        if (value instanceof Integer) {
                            floatValue = ((Integer) value).floatValue();
                        } else if (value instanceof Long) {
                            floatValue = ((Long) value).floatValue();
                        } else if (value instanceof Double) {
                            floatValue = ((Double) value).floatValue();
                        } else if (value instanceof Float) {
                            floatValue = (Float) value;
                        }
                        entries.add(new Entry(entries.size(), floatValue));
                    }
                }
                //send the new values.
                //updates each time there is a new value added to database.
                updateChart(entries, fieldName, unit);
            }

            //error handling.
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", error.getMessage());
            }
        });
    }
    //update function
    private void updateChart(List<Entry> entries, String label, String unit) {
        LineDataSet dataSet = new LineDataSet(entries, label + " (" + unit + ")");
        //customized so that the chart only shows a line
        dataSet.setColor(getColorForLabel(label));  //color depends on the type of measurement
        dataSet.setDrawCircles(false);              //hides the dots
        //dataSet.setCircleRadius(4f);              //you can have dots, now no need.
        dataSet.setLineWidth(1f);                   //line width

        //setup the chart with the data
        LineData lineData = new LineData(dataSet);

        //x axis details
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);  //on the bottom of chart
        xAxis.setLabelRotationAngle(-15); //rotate the label
        xAxis.setLabelCount(4);
        xAxis.setAvoidFirstLastClipping(true); //no clipping
        //should list the time as such:
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Convert sample index to minutes (30 seconds per sample = 0.5 minutes)
                //float minutes = value * 0.5f;
                int hours = (int) (value / 60);
                int mins = (int) (value % 60);
                return String.format(Locale.getDefault(), "%02d:%02d", hours, mins);
            }
        });

        //y axis details
        chart.getAxisRight().setEnabled(false); //remove the lhs axis
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        //only show the units you need according to the chart you are on
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1f %s", value, unit);
            }
        });

        //set the legend on top of the chart
        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        //chart settings
        chart.setData(lineData);
        chart.setExtraBottomOffset(30f);
        chart.invalidate();


    }

    //this will tell the color we want.
    private int getColorForLabel(String label) {
        switch (label) {
            case "co2":
                return Color.YELLOW;
            case "height":
                return Color.RED;
            case "humidity":
                return Color.GREEN;
            case "temperature":
                return Color.CYAN;
            default: return Color.BLACK;
        }
    }
}