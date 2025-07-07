package com.example.mainactivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class WiFiActivity extends AppCompatActivity {

    private EditText etSsid, etPassword;
    private BluetoothSocket btSocket;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 100;
    private BluetoothSocket bluetoothSocket;
    boolean finish = false;
    private Button complete, btnSubmit;
    String ip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi);

        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#cc8e90"));
        actionBar.setBackgroundDrawable(colorDrawable);
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>Wifi Connection </font>"));

        etSsid = findViewById(R.id.etSsid);
        etPassword = findViewById(R.id.etPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        complete = findViewById(R.id.wifi_complete_button);

        // Get Bluetooth socket from intent
        btSocket = BluetoothConnectionManager.getInstance().getSocket();
        if (btSocket == null || !btSocket.isConnected()) {
            Toast.makeText(this, "FAILED CONNECTION TO BLUETOOTH", Toast.LENGTH_SHORT).show();
        }

        btnSubmit.setOnClickListener(v -> {
            String ssid = etSsid.getText().toString();
            String password = etPassword.getText().toString();

            if (btSocket != null && btSocket.isConnected()) {
                try {
                    OutputStream out = btSocket.getOutputStream();
                    out.write((ssid + "|" + password + "\n").getBytes());

                    InputStream in = btSocket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String response = reader.readLine();

                    if (response != null && response.startsWith("WIFI_SUCCESS")) {
                        ip = response.split("\\|")[1];
                        finish = true;
                        Toast.makeText(this, "Successfully connected to the internet!", Toast.LENGTH_SHORT).show();
                        
                    } else {
                        Toast.makeText(this, "Failed to configure WiFi", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "Connection error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Not connected to device", Toast.LENGTH_SHORT).show();
            }
        });

        complete.setOnClickListener(v -> {
            if (finish){
                Intent intent = new Intent (this, MainActivity.class);
                intent.putExtra("ESP32_IP", ip);
                startActivity(intent);
                finish();
            } else{
                Toast.makeText(this, "You need to connect to the internet to complete setup!", Toast.LENGTH_SHORT).show();
            }
        });

    }

}