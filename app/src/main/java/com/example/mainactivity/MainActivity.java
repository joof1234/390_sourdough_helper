package com.example.mainactivity;

import android.content.Intent;
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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Button bluetooth;
    TextView tvExampledata;
    private Handler handler = new Handler();
    private String esp32Ip;
    private boolean isRunning = true;


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
    }

    private void setup() {
        bluetooth = findViewById(R.id.gotobluetooth);
        tvExampledata = findViewById(R.id.wifidataexample);


        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        });


    }

    private void toolbar() {
        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#cc8e90"));
        actionBar.setBackgroundDrawable(colorDrawable);
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>Main Page </font>"));
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
                    }

                    String data = reader.readLine();
                    if (data != null) {
                        final String finalData = "Distance: " + data + " cm";
                        handler.post(() -> tvExampledata.setText(finalData));
                    }

                } catch (Exception e) {
                    Log.e("MainActivity", "Error", e);
                    handler.post(() -> tvExampledata.setText("Waiting for data..."));

                    // Close broken connection
                    try {
                        if (socket != null) socket.close();
                    } catch (IOException ioException) {
                        Log.e("MainActivity", "Error closing socket", ioException);
                    }

                    // Wait before reconnecting
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            // Clean up when done
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
        isRunning = false; // Stop the data receiver thread
    }


}