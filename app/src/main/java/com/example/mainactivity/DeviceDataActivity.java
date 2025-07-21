package com.example.mainactivity;

import static androidx.core.text.HtmlCompat.fromHtml;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DeviceDataActivity extends AppCompatActivity {
    private TextView tvDeviceData, tvDeviceMessage;
    private String deviceIp;
    private Handler handler = new Handler();
    private boolean isRunning = true;
    private Socket socket;
    private BufferedReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_data);

        setupUI();

        startDataReceiver();
    }

    private void setupUI(){
        tvDeviceData = findViewById(R.id.tvDeviceData);
        deviceIp = getIntent().getStringExtra("DEVICE_IP");
        tvDeviceMessage = findViewById(R.id.device_msg);


        tvDeviceMessage.setText("Device from " + deviceIp);

        //setup action bar
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            ColorDrawable colorDrawable = new ColorDrawable(getColor(R.color.primary_color));
            actionBar.setBackgroundDrawable(colorDrawable);
            actionBar.setTitle(fromHtml("Data Dashboard",getColor(R.color.on_primary_color)));
        }

        //graphed data button click
        findViewById(R.id.buttonGraphedData).setOnClickListener(v -> {
            Intent intent = new Intent(DeviceDataActivity.this, DataGraphActivity.class);
            startActivity(intent);
        });
    }

    //receive data from the server
    private void startDataReceiver() {
        new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(deviceIp, 8080), 5000);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (isRunning) {
                    try {
                        String data = reader.readLine();
                        if (data != null) {
                            final String finalData = "Temp and Humidity: " + data;
                            handler.post(() -> tvDeviceData.setText(finalData));
                        }
                    } catch (IOException e) {
                        handler.post(() -> tvDeviceData.setText("Connection error"));
                        reconnect();
                    }
                }
            } catch (IOException e) {
                handler.post(() -> tvDeviceData.setText("Failed to connect"));
            }
        }).start();
    }

    //reconnect to the active server.
    private void reconnect() {
        try {
            Thread.sleep(1000);
            if (isRunning) {
                startDataReceiver();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    //confirmation stop wifi connection
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        try {
            if (socket != null) socket.close();
            if (reader != null) reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}