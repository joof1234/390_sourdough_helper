package com.example.mainactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private Button bluetooth;
    private TextView tvExampledata, summary;
    private Handler handler = new Handler();
    private String esp32Ip;
    private boolean isRunning = true;
    private static final String PREFS_NAME = "ESP32Prefs";
    private static final String PREF_IP_ADDRESS = "esp32_ip";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setup();
        toolbar();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ESP32_IP")) {
            esp32Ip = intent.getStringExtra("ESP32_IP");
            Log.d("MainActivity", "Connecting to ESP32 at: " + esp32Ip);
            startDataReceiver();
        } else {
            Log.e("MainActivity", "No ESP32 IP provided");
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedIp = prefs.getString(PREF_IP_ADDRESS, null);
        esp32Ip = getIntent().getStringExtra("ESP32_IP");

        if (esp32Ip == null && savedIp != null) {
            esp32Ip = savedIp;
            Log.d("MainActivity", "Using saved IP: " + esp32Ip);
        }

        if (esp32Ip != null) {
            Log.d("MainActivity", "Connecting to ESP32 at: " + esp32Ip);
            startDataReceiver();
        } else {
            Log.e("MainActivity", "No ESP32 IP provided");
            summary.setText("No devices connected!");
        }

    }

    private void setup() {
        bluetooth = findViewById(R.id.gotobluetooth);
        tvExampledata = findViewById(R.id.wifidataexample);
        summary = findViewById(R.id.main_summary);

        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        });

        Button btnReset = findViewById(R.id.reset_button);
        btnReset.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().remove(PREF_IP_ADDRESS).apply();
            Toast.makeText(this, "Cleared devices!", Toast.LENGTH_SHORT).show();
            finish();
        });


    }

    private void toolbar() {
        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#cc8e90"));
        actionBar.setBackgroundDrawable(colorDrawable);
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>Main Page </font>"));
    }

    private void saveIpAddress(String ip) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(PREF_IP_ADDRESS, ip).apply();
    }
    private void startDataReceiver() {
        new Thread(() -> {
            Socket socket = null;
            BufferedReader reader = null;

            while (isRunning) {
                try {
                    if (socket == null || socket.isClosed()) {
                        Log.d("MainActivity", "Attempting to connect...");
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(esp32Ip, 8080), 5000);
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        Log.d("MainActivity", "Connected to ESP32");
                        summary.setText(1 + " Devices connected");
                    }

                    String data = reader.readLine();
                    if (data != null) {
                        final String finalData = "Distance: " + data + " cm";
                        handler.post(() -> tvExampledata.setText(finalData));
                    }

                } catch (Exception e) {
                    Log.e("MainActivity", "Error", e);
                    handler.post(() -> tvExampledata.setText("Waiting for data..."));

                    if (esp32Ip != null) {
                        saveIpAddress(esp32Ip);
                    }

                    //close broken connection
                    try {
                        if (socket != null) socket.close();
                    } catch (IOException ioException) {
                        Log.e("MainActivity", "Error closing socket", ioException);
                    }

                    //wait for reconnection
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            //clean up when done
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                Log.e("MainActivity", "Error closing socket", e);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false; //stop the data receiver thread
    }


}