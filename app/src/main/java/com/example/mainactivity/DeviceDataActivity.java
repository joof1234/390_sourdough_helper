package com.example.mainactivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeviceDataActivity extends AppCompatActivity {
    private TextView tvDeviceMessage;
    private String deviceMac;
    private Button databaseReset;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_data);

        //we are not using IP anymore. we use mac for database recognition
        deviceMac = getIntent().getStringExtra("DEVICE_MAC");

        //text views about which device is connected.
        tvDeviceMessage = findViewById(R.id.device_msg);
        tvDeviceMessage.setText("Device from " + deviceMac);

        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#cc8e90"));
            actionBar.setBackgroundDrawable(colorDrawable);
            actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>Starter Dashboard </font>"));
        }

        //setup the pager and the tab layout
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        //the adapter is each different charts, give the mac address
        SensorPagerAdapter adapter = new SensorPagerAdapter(this, deviceMac);
        viewPager.setAdapter(adapter);

        //the tabs are used to navigate between charts.
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(adapter.getTabTitle(position));
        }).attach();

        //reset button, resets the data on the
        databaseReset = findViewById(R.id.db_reset);
        databaseReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetDeviceData();
            }
        });
//        //database test move later
//        mDatabase = FirebaseDatabase.getInstance().getReference("sensors/" + deviceMac);
//        mDatabase.addValueEventListener(new ValueEventListener() {
//        @Override
//        public void onDataChange(@NonNull DataSnapshot snapshot) {
//            List<Entry> dataPoints = new ArrayList<>();
//
//            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
//                Float temp = childSnapshot.child("sht31_temp").getValue(Float.class);
//                Long timestamp = childSnapshot.child("timestamp").getValue(Long.class);
//
//                if (temp != null && timestamp != null) {
//                    // Convert timestamp to hours/minutes for X-axis
//                    String time = convertTimestampToTime(timestamp);
//                    dataPoints.add(new Entry(dataPoints.size(), temp)); // X = index, Y = temp
//                }
//            }
//            updateChart(dataPoints); // Update the chart (see next step)
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError error) {
//            Log.e("Firebase", "Error fetching data", error.toException());
//        }
//    });
    }

    private void resetDeviceData() {
        //get the database reference for the mac_address
        DatabaseReference deviceReadingsRef = FirebaseDatabase.getInstance()
                .getReference("sensors/" + deviceMac);

        //ask the user the confirm delete
        new AlertDialog.Builder(this)
                .setTitle("Clear Device Readings")
                .setMessage("This will reset all data.\n Do you wish to continue?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    //removes the lid device's data in the firebase only
                    deviceReadingsRef.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Cleared data", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to clear data" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
//    Helper method to format timestamp
//    private String convertTimestampToTime(long timestamp) {
//        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
//        return sdf.format(new Date(timestamp * 1000L)); // Convert seconds to milliseconds
//    }
//
//    private void updateChart(List<Entry> dataPoints) {
//        LineChart lineChart = findViewById(R.id.temperatureChart);
//
//        LineDataSet dataSet = new LineDataSet(dataPoints, "Temperature (°C)");
//        dataSet.setColor(Color.RED);
//        dataSet.setValueTextColor(Color.BLACK);
//        dataSet.setLineWidth(2f);
//
//        LineData lineData = new LineData(dataSet);
//        lineChart.setData(lineData);
//
//        // Customize X-axis (time)
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//                return String.format("%.0f:00", value); // Format as "HH:00"
//            }
//        });
//
//        lineChart.invalidate(); // Refresh chart
//    }
}