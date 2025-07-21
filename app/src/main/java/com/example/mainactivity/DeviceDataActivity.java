package com.example.mainactivity;

import static androidx.core.text.HtmlCompat.fromHtml;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DeviceDataActivity extends AppCompatActivity {
    private TextView tvDeviceData, tvDeviceMessage;
    private String deviceIp, deviceMac;
    private Handler handler = new Handler();
    private boolean isRunning = true;
    private Socket socket;
    private BufferedReader reader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_data);

        setupUI();
        toolbar_setup();
        startDataReceiver();
    }

    private void toolbar_setup() {
        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#cc8e90"));
        actionBar.setBackgroundDrawable(colorDrawable);
        //different name, profile page
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>Starter Dashboard </font>"));
        actionBar.setDisplayHomeAsUpEnabled(true);
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
            intent.putExtra("DEVICE_MAC",deviceMac);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_data_toolbar, menu);
        return true;
    }

    //options to select in the toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //first option is to sort students by id or surname
        if (item.getItemId()== android.R.id.home){
            //back button
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
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