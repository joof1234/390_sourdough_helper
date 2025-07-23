package com.example.mainactivity;

import static androidx.core.text.HtmlCompat.fromHtml;

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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
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
    boolean finish = false;
    private Button complete, btnSubmit;
    String ip, MacAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi);

        //setup action bar
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            ColorDrawable colorDrawable = new ColorDrawable(getColor(R.color.primary_color));
            actionBar.setBackgroundDrawable(colorDrawable);
            actionBar.setTitle(fromHtml("WiFi Connection",getColor(R.color.on_primary_color)));
        }

        //widgets
        etSsid = findViewById(R.id.etSsid);
        etPassword = findViewById(R.id.etPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        complete = findViewById(R.id.wifi_complete_button);

        //get the bluetooth socket from the manager class we setup
        btSocket = BluetoothConnectionManager.getInstance().getSocket();
        if (btSocket == null || !btSocket.isConnected()) {
            Toast.makeText(this, "FAILED CONNECTION TO BLUETOOTH", Toast.LENGTH_SHORT).show();
        }

        //when you press this it sends the data to the device via bluetooth
        btnSubmit.setOnClickListener(v -> {
            String ssid = etSsid.getText().toString();
            String password = etPassword.getText().toString();

            //if you are connected send.
            if (btSocket != null && btSocket.isConnected()) {
                try {
                    //send a data with delimter of |
                    OutputStream out = btSocket.getOutputStream();
                    out.write((ssid + "|" + password + "\n").getBytes());

                    //get the feedback like a "connected message"
                    InputStream in = btSocket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String response = reader.readLine();

                    if (response != null && response.startsWith("WIFI_SUCCESS")) {
                        //delimit everything.
                        ip = response.split("\\|")[1];
                        MacAddress = response.split("\\|")[2];
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

        //complete button only if you are done bluetooth connecting.
        complete.setOnClickListener(v -> {
            if (finish) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("ESP32_IP", ip);
                intent.putExtra("ESP32_MAC", MacAddress); //send MAC address
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Connect to WiFi first!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wifi_toolbar, menu);
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

}