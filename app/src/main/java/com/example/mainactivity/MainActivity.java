package com.example.mainactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ListView lvDevices;
    private ArrayAdapter<String> deviceAdapter;
    private ArrayList<String> connectedDevices = new ArrayList<>();
    private Set<String> deviceIps = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#cc8e90"));
        actionBar.setBackgroundDrawable(colorDrawable);
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>List of Starters </font>"));

        setupViews();
        loadConnectedDevices();
        checkNewDevice();
    }

    private void setupViews() {
        lvDevices = findViewById(R.id.lvDevices);
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, connectedDevices);
        lvDevices.setAdapter(deviceAdapter);

        lvDevices.setOnItemClickListener((parent, view, position, id) -> {
            String deviceInfo = connectedDevices.get(position);
            String deviceIp = deviceInfo.split(" - ")[1]; // Extract IP from display string

            Intent intent = new Intent(MainActivity.this, DeviceDataActivity.class);
            intent.putExtra("DEVICE_IP", deviceIp);
            startActivity(intent);
        });

        Button btnAddDevice = findViewById(R.id.gotobluetooth);
        btnAddDevice.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
            startActivity(intent);
        });

        Button btnReset = findViewById(R.id.reset_button);
        btnReset.setOnClickListener(v -> {
            clearConnectedDevices();
        });
    }

    private void loadConnectedDevices() {
        SharedPreferences prefs = getSharedPreferences("DEVICE_PREFS", MODE_PRIVATE);
        Set<String> savedDevices = prefs.getStringSet("CONNECTED_DEVICES", new HashSet<>());
        connectedDevices.clear();
        connectedDevices.addAll(savedDevices);
        deviceAdapter.notifyDataSetChanged();
    }

    private void checkNewDevice() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ESP32_IP")) {
            String newIp = intent.getStringExtra("ESP32_IP");
            addConnectedDevice(newIp);
        }
    }

    private void addConnectedDevice(String ip) {
        String deviceInfo = "ESP32 Device - " + ip;
        if (!connectedDevices.contains(deviceInfo)) {
            connectedDevices.add(deviceInfo);
            saveConnectedDevices();
            deviceAdapter.notifyDataSetChanged();
        }
    }

    private void saveConnectedDevices() {
        SharedPreferences prefs = getSharedPreferences("DEVICE_PREFS", MODE_PRIVATE);
        Set<String> devicesSet = new HashSet<>(connectedDevices);
        prefs.edit().putStringSet("CONNECTED_DEVICES", devicesSet).apply();
    }

    private void clearConnectedDevices() {
        connectedDevices.clear();
        saveConnectedDevices();
        deviceAdapter.notifyDataSetChanged();
    }
}