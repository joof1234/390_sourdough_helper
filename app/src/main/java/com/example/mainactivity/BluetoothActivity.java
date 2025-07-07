package com.example.mainactivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> deviceAdapter;
    private ArrayList<String> deviceList;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    TextView tvStatus;
    Button btnScan, btnSend, complete;
    ListView lvDevices;
    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        btnScan = findViewById(R.id.btnScan);
        btnSend = findViewById(R.id.btnSend);
        complete = findViewById(R.id.complete_bluetooth);
        lvDevices = findViewById(R.id.lvDevices);
        tvStatus = findViewById(R.id.tvStatus);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            tvStatus.setText("Bluetooth not supported");
            btnScan.setEnabled(false);
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#cc8e90"));
        actionBar.setBackgroundDrawable(colorDrawable);
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>Bluetooth connection </font>"));

        // Initialize device list
        deviceList = new ArrayList<>();
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        lvDevices.setAdapter(deviceAdapter);

        // Scan button click handler
        btnScan.setOnClickListener(v -> {
            if (checkBluetoothPermissions()) {
                scanDevices();
            }
        });

        complete.setOnClickListener(v -> {
            if (connected){
                Intent intent = new Intent (BluetoothActivity.this, WiFiActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Connect to a device first!", Toast.LENGTH_SHORT).show();
            }
        });

        // ListView item click handler
        lvDevices.setOnItemClickListener((parent, view, position, id) -> {
            if (checkBluetoothPermissions()) {
                String deviceInfo = deviceList.get(position);
                String deviceAddress = deviceInfo.substring(deviceInfo.length() - 17);

                try {
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                    connectToDevice(device);
                    String deviceName;
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        deviceName = device.getName();
                    } else {
                        deviceName = "Unknown Device";
                    }
                    tvStatus.setText("Connecting to " + deviceName);
                    btnSend.setEnabled(true);
                } catch (IllegalArgumentException e) {
                    tvStatus.setText("Invalid device address");
                }
            }
        });

        // Send button click handler
        btnSend.setOnClickListener(v -> {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                try {
                    String message = "Hello ESP32!\n";
                    outputStream.write(message.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush(); // Force send
                    tvStatus.setText("Message sent: " + message);
                } catch (IOException e) {
                    tvStatus.setText("Error sending message");
                }
            }
        });
    }

    private boolean checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void scanDevices() {
        if (bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            pairedDevices = bluetoothAdapter.getBondedDevices();
            deviceList.clear();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().toLowerCase().contains("esp")){
                    deviceList.add(device.getName() + "\n" + device.getAddress());
                }
            }
            deviceAdapter.notifyDataSetChanged();
            TextView tvStatus = findViewById(R.id.tvStatus);
            tvStatus.setText("Found " + deviceList.size() + " devices");
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        new Thread(() -> {
            try {
                // Existing connection code
                if (ActivityCompat.checkSelfPermission(BluetoothActivity.this,
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();

                BluetoothConnectionManager.getInstance().setSocket(bluetoothSocket);

                // Add the distance reading receiver
                InputStream inputStream = bluetoothSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                runOnUiThread(() -> {
                    tvStatus.setText("Connected to " + device.getName());
                    btnSend.setEnabled(true);
                    connected = true;
                });

//                // Continuous reading loop
//                while (true) {
//                    try {
//                        String distanceData = reader.readLine();
//                        runOnUiThread(() -> {
//                            tvStatus.setText("Distance: " + distanceData);
//                        });
//                    } catch (IOException e) {
//                        runOnUiThread(() -> {
//                            tvStatus.setText("Disconnected");
//                        });
//                        break;
//                    }
//                }

            } catch (IOException e) {
                runOnUiThread(() -> {
                    tvStatus.setText("Connection failed: " + e.getMessage());
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with Bluetooth operations
                scanDevices();
            } else {
                Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
