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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DeviceDataActivity extends AppCompatActivity {
    private TextView tvDeviceData, tvDeviceMessage;
    private String deviceIp, deviceMac;
    private Button btnStop, btnStart, btnNew, btnNextDay;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_data);

        setupUI();
        toolbar_setup();
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
        deviceIp = getIntent().getStringExtra("DEVICE_IP");
        deviceMac = getIntent().getStringExtra("DEVICE_MAC");

        tvDeviceMessage = findViewById(R.id.device_msg);
        tvDeviceData = findViewById(R.id.tvDeviceData);

        tvDeviceMessage.setText("Device IP: " + deviceIp);
        tvDeviceData.setText("Device MAC Address: " + deviceMac);

        btnStart = findViewById(R.id.device_data_start_button);
        btnStop = findViewById(R.id.device_data_stop_button);
        btnNew = findViewById(R.id.device_data_new_button);
        btnNextDay = findViewById(R.id.device_data_new_day);

        databaseReference = FirebaseDatabase.getInstance().getReference("sensors/" + deviceMac);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("general")
                        .child("current_day")
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DataSnapshot snapshot = task.getResult();
                                if (snapshot.exists()) {
                                    Integer currentDay = snapshot.getValue(Integer.class);
                                    if (currentDay != null && currentDay == 0) {
                                        Toast.makeText(DeviceDataActivity.this,
                                                "You need to start a new dough first!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("general")
                        .child("current_day")
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DataSnapshot snapshot = task.getResult();
                                if (snapshot.exists()) {
                                    Integer currentDay = snapshot.getValue(Integer.class);
                                    if (currentDay != null && currentDay == 0) {
                                        Toast.makeText(DeviceDataActivity.this,
                                                "You need to start a new dough first!",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        databaseReference.child("general")
                                                .child("enable")
                                                .setValue(false)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(DeviceDataActivity.this,
                                                            "Device disabled successfully",
                                                            Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(DeviceDataActivity.this,
                                                            "Failed to disable device: " + e.getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                }
                            }
                        });
            }
        });
        btnNew.setOnClickListener(v -> {
            databaseReference.child("general").child("current_day").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        Integer currentDay = snapshot.getValue(Integer.class);
                        if (currentDay != null && currentDay == 0) {
                            //move the user to the intent to 1st day, and when they complete that intent, they should come back to this activity
                            //and the device should have it enabled.
                            databaseReference.child("general").child("current_day").setValue(1).addOnSuccessListener(aVoid -> {
                                Toast.makeText(DeviceDataActivity.this, "Device set to day 1!", Toast.LENGTH_SHORT).show();
                                databaseReference.child("general").child("enable").setValue(true).addOnFailureListener(e -> {
                                    Toast.makeText(DeviceDataActivity.this, "Failed to enable device: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }).addOnFailureListener(e -> {
                                Toast.makeText(DeviceDataActivity.this, "Failed to set the device date!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            Toast.makeText(DeviceDataActivity.this,
                                    "Confirm Restart??",
                                    Toast.LENGTH_SHORT).show();
                            //clear all day data, move the user to the first feeding day. once they are done, it enables the sensors.
                            //TODO: You need to confirm restart when you press this button.
                        }
                    }
                }
            });
        });
        btnNextDay.setOnClickListener(v -> {
            //check with the algorithm if the user's data is good.
            //get the maximum height, co2, and time, and do comparison

            //if everything is met, disable the device, and move the user to the next feeding day.
            //if not met, prompt the user to be sure to press the next day.

            //then, for either cases start an intent to a feeding activity for the day
            //when the user is done, returns to this activity, and enables the device again.

            //if the user is at day 7, then something else happens. maybe we show them recipes or we tell them that this dough is complete,
            //and that you can't continue, or tell them how to maintain it, but the lid is not used anymore after this.
        });

        //TODO: MAKE A NEXT DAY BUTTON.
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
            //in the xml we use Singletop to prevent loss of data per pressing back buttons betweent
            //activities.
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_data_toolbar, menu);
        return true;
    }

    //options to select in the toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()== android.R.id.home){
            //back button
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        else if (item.getItemId() == R.id.action_setup_connection){
            Intent intent = new Intent(DeviceDataActivity.this, BluetoothActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}