package com.example.mainactivity;

import static android.view.View.GONE;
import static androidx.core.text.HtmlCompat.fromHtml;
import static androidx.core.util.TimeUtils.formatDuration;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class DeviceDataActivity extends AppCompatActivity {
    private TextView tvstarthumidity, tvstartco2, tvstarttemperature, tvstartheight;
    private TextView tvmaxhumidity, tvmaxco2, tvmaxtemperature, tvmaxheight;
    //general info textviews
    private TextView tvhours, tvday, tvready, tvstartername;

    private float max_humidity, max_co2, max_temperature, max_height;
    private String deviceName = "Missing name!";
    private String deviceIp, deviceMac;
    private Button btnStop, btnStart, btnNew, btnNextDay, btnGraphData;
    private DatabaseReference databaseReference;
    private Integer currentDay;
    private long firstTimestamp = -1;
    private long lastTimestamp = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_data);

        setupUI();
        toolbar_setup();
        fetchStartData();
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

        //summary textview
        //should show the start data,
        //should show the peak data
        //should show how long it has been running for
        //ready or not status

        //start data
        tvstarthumidity = findViewById(R.id.device_data_start_humidity);
        tvstartco2= findViewById(R.id.device_data_start_co2);
        tvstarttemperature= findViewById(R.id.device_data_start_temp);
        tvstartheight= findViewById(R.id.device_data_start_height);
        //max data
        tvmaxhumidity = findViewById(R.id.device_data_max_humidity);
        tvmaxco2 = findViewById(R.id.device_data_max_co2);
        tvmaxtemperature = findViewById(R.id.device_data_max_temp);
        tvmaxheight= findViewById(R.id.device_data_max_height);
        //general data
        tvhours = findViewById(R.id.device_data_duration_textview);
        tvday = findViewById(R.id.device_data_current_day_textview);
        tvready = findViewById(R.id.device_data_result_textview);
        tvstartername = findViewById(R.id.device_data_starter_name);

        databaseReference = FirebaseDatabase.getInstance().getReference("sensors/" + deviceMac);

        //buttons
        btnStart = findViewById(R.id.device_data_start_button);
        btnNew = findViewById(R.id.device_data_new_button);
        //btnNextDay = findViewById(R.id.device_data_new_day);

//        //set device name from firebase
//        databaseReference.child("general").child("device_name").get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                String deviceName = task.getResult().getValue(String.class);
//                tvstartername.setText(deviceName);
//            } else {
//                //cant retrieve name
//            }
//        });

        btnStart.setOnClickListener(v -> {
            databaseReference.child("general").child("current_day").get().addOnCompleteListener(task -> {
                if (!task.isSuccessful() || !task.getResult().exists()) {
                    Toast.makeText(this, "Failed to check current day", Toast.LENGTH_SHORT).show();
                    return;
                }

                Integer currentDay = task.getResult().getValue(Integer.class);
                if (currentDay == null || currentDay == 0) {
                    Toast.makeText(this, "You need to start a new dough first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                databaseReference.child("general").child("enable").get().addOnCompleteListener(enableTask -> {
                    if (!enableTask.isSuccessful() || !enableTask.getResult().exists()) {
                        Toast.makeText(this, "Failed to check device status", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Boolean isEnabled = enableTask.getResult().getValue(Boolean.class);
                    boolean newState = !Boolean.TRUE.equals(isEnabled); // Toggle the state

                    // Update UI first for responsiveness
                    if (newState) {
                        btnStart.setText("DISABLE LID");
                        btnStart.setBackgroundColor(Color.parseColor("#B9375D")); // Red for disable
                    } else {
                        btnStart.setText("ENABLE LID");
                        btnStart.setBackgroundColor(Color.parseColor("#689B8A")); // Green for enable
                    }

                    // Update database
                    databaseReference.child("general").child("enable").setValue(newState)
                            .addOnSuccessListener(aVoid -> {
                                String message = newState ? "Device enabled!" : "Device disabled!";
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Operation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                // Revert UI if update fails
                                if (Boolean.TRUE.equals(isEnabled)) {
                                    btnStart.setText("DISABLE LID");
                                    btnStart.setBackgroundColor(Color.parseColor("#B9375D"));
                                } else {
                                    btnStart.setText("ENABLE LID");
                                    btnStart.setBackgroundColor(Color.parseColor("#689B8A"));
                                }
                            });
                });
            });
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

//        btnNextDay.setOnClickListener(v -> {
//            //check with the algorithm if the user's data is good.
//            //get the maximum height, co2, and time, and do comparison
//
//            //if everything is met, disable the device, and move the user to the next feeding day.
//            //if not met, prompt the user to be sure to press the next day.
//
//            //then, for either cases start an intent to a feeding activity for the day
//            //when the user is done, returns to this activity, and enables the device again.
//
//            //if the user is at day 7, then something else happens. maybe we show them recipes or we tell them that this dough is complete,
//            //and that you can't continue, or tell them how to maintain it, but the lid is not used anymore after this.
//        });

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
        btnGraphData = findViewById(R.id.buttonGraphedData);
        btnGraphData.setOnClickListener(v -> {
            Intent intent = new Intent(DeviceDataActivity.this, DataGraphActivity.class);
            intent.putExtra("DEVICE_MAC",deviceMac);
            intent.putExtra("DAY", currentDay);
            //in the xml we use Singletop to prevent loss of data per pressing back buttons betweent
            //activities.
            startActivity(intent);
        });
        //feeding instructions button click
        findViewById(R.id.buttonFeedingInstructions).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("MAC", deviceMac);
            //open feeding dialog fragment
            FeedingDialogFragment feedingDialogFragment = new FeedingDialogFragment();
            feedingDialogFragment.setArguments(args);
            feedingDialogFragment.show(getSupportFragmentManager(), "FeedingDialogFragment");
        });
    }
    private void checkEnabled(){
        databaseReference.child("general").child("enable").get().addOnCompleteListener(enableTask -> {
            if (!enableTask.isSuccessful() || !enableTask.getResult().exists()) {
                Toast.makeText(this, "Failed to check device status", Toast.LENGTH_SHORT).show();
                return;
            }

            Boolean isEnabled = enableTask.getResult().getValue(Boolean.class);
            boolean newState = Boolean.TRUE.equals(isEnabled); // Toggle the state

            // Update UI first for responsiveness
            if (newState) {
                btnStart.setText("DISABLE LID");
                btnStart.setBackgroundColor(Color.parseColor("#B9375D")); // Red for disable
            } else {
                btnStart.setText("ENABLE LID");
                btnStart.setBackgroundColor(Color.parseColor("#689B8A")); // Green for enable
            }
        });
    }
    private void fetchStartData() {
        databaseReference.child("general").child("current_day").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentDay = snapshot.getValue(Integer.class);
                    if (currentDay != null && currentDay > 0) {
                        // Fetch start data for the current day
                        databaseReference.child("day_" + currentDay).child("start_data").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot startSnapshot) {
                                if (startSnapshot.exists()) {
                                    // Get start values
                                    System.out.println("I HAVE FOUND THE START DATA!!! DAY IS: " + currentDay);
                                    Float startHumidity = startSnapshot.child("humidity").getValue(Float.class);
                                    Float startCo2 = startSnapshot.child("co2").getValue(Float.class);
                                    Float startTemperature = startSnapshot.child("temperature").getValue(Float.class);
                                    Float startHeight = startSnapshot.child("height").getValue(Float.class);

                                    System.out.println("1: "  + startHumidity + " 2: " + startCo2 + " 3: " + startTemperature + " 4: "+ startHeight);
                                    // Update UI with start values
                                    if (startHumidity != null) {
                                        tvstarthumidity.setText(String.format("%.1f%%", startHumidity));
                                    }
                                    if (startCo2 != null) {
                                        tvstartco2.setText(String.format("%.1f ppm", startCo2));
                                    }
                                    if (startTemperature != null) {
                                        tvstarttemperature.setText(String.format("%.1f°C", startTemperature));
                                    }
                                    if (startHeight != null) {
                                        tvstartheight.setText(String.format("%.1f mm", ((180.0f -startHeight)/10.0f)));
                                    }
                                    if (currentDay == 8){
                                        tvday.setText("The dough is currently at Day 7+");
                                        btnGraphData.setVisibility(GONE);
                                    }else{
                                        tvday.setText("The dough is currently at Day " + currentDay);
                                    }

                                    fetchMaxData(currentDay);
                                    setupTimestampTracker();
                                    checkEnabled();
                                    databaseReference.child("general").child("device_name").addListenerForSingleValueEvent(new ValueEventListener() {
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                deviceName = snapshot.getValue(String.class);
                                                tvstartername.setText(deviceName);
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(DeviceDataActivity.this, "Failed to read current day: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(DeviceDataActivity.this, "Failed to read start data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeviceDataActivity.this, "Failed to read current day: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupTimestampTracker() {
        //get reference
        DatabaseReference deviceReadingsRef = FirebaseDatabase.getInstance()
                .getReference("sensors/" + deviceMac + "/day_" + currentDay);

        //track data changes on the database
        deviceReadingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    tvhours.setText("No data available for Day " + currentDay);
                    return;
                }

                // Count the number of data points
                long dataPointCount = snapshot.getChildrenCount();

                // Calculate duration in minutes
                long durationMinutes = dataPointCount-1;

                // Update the text view
                updateDurationInfo(durationMinutes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeviceDataActivity.this, "Error reading data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateDurationInfo(long durationMinutes) {
        if (durationMinutes == 0) {
            tvhours.setText("No dough started yet");
            return;
        }

        // Convert minutes to hours and minutes
        long hours = durationMinutes / 60;
        long minutes = durationMinutes % 60;

        // Format the duration text
        String durationText;
        if (hours > 0) {
            durationText = String.format(Locale.getDefault(), "%d hours, %d minutes", hours, minutes);
        } else {
            durationText = String.format(Locale.getDefault(), "%d minutes", minutes);
        }

        // Update the TextView
        tvhours.setText("Duration: " + durationText);
    }
    private String formatDuration(long seconds) {
        long days = seconds / (24 * 3600);
        long hours = (seconds % (24 * 3600)) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (days > 0) {
            return String.format(Locale.getDefault(), "%d days, %d hours", days, hours);
        } else if (hours > 0) {
            return String.format(Locale.getDefault(), "%d hours, %d minutes", hours, minutes);
        } else if (minutes > 0) {
            return String.format(Locale.getDefault(), "%d minutes, %d seconds", minutes, secs);
        } else {
            return String.format(Locale.getDefault(), "%d seconds", secs);
        }
    }
    private void fetchMaxData(int currentDay) {
        DatabaseReference dayRef = databaseReference.child("day_" + currentDay);

        // Create a holder class for our max values
        class MaxValues {
            float humidity = Float.MIN_VALUE;
            float co2 = Float.MIN_VALUE;
            float temperature = Float.MIN_VALUE;
            float height = Float.MAX_VALUE;
        }

        final MaxValues maxValues = new MaxValues();

        dayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot daySnapshot) {
                if (daySnapshot.exists()) {
                    //skip start data
                    for (DataSnapshot entrySnapshot : daySnapshot.getChildren()) {
                        if (entrySnapshot.getKey().equals("start_data")) {
                            continue; // Skip the start node
                        }

                        //get values from entry
                        Float humidity = entrySnapshot.child("humidity").getValue(Float.class);
                        Float co2 = entrySnapshot.child("co2").getValue(Float.class);
                        Float temperature = entrySnapshot.child("temperature").getValue(Float.class);
                        Float height = entrySnapshot.child("height").getValue(Float.class);

                        //update the values
                        if (humidity != null && humidity > maxValues.humidity) {
                            maxValues.humidity = humidity;
                        }
                        if (co2 != null && co2 > maxValues.co2) {
                            maxValues.co2 = co2;
                        }
                        if (temperature != null && temperature > maxValues.temperature) {
                            maxValues.temperature = temperature;
                        }
                        if (height != null && height < maxValues.height) {
                            maxValues.height = height;
                        }
                    }

                    // Update UI with max values
                    runOnUiThread(() -> {
                        tvmaxhumidity.setText(String.format("%.1f%%", maxValues.humidity));
                        tvmaxco2.setText(String.format("%.1f ppm", maxValues.co2));
                        tvmaxtemperature.setText(String.format("%.1f°C", maxValues.temperature));
                        tvmaxheight.setText(String.format("%.1f mm", ((180.0f - maxValues.height)/10.0f)));

                        // Update class variables
                        max_humidity = maxValues.humidity;
                        max_co2 = maxValues.co2;
                        max_temperature = maxValues.temperature;
                        max_height = maxValues.height;
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() ->
                        Toast.makeText(DeviceDataActivity.this,
                                "Failed to read max data: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }
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
        else if (item.getItemId() == R.id.action_rename_device){
            //open dialog fragment to rename device
            DeviceNameDialogFragment deviceNameDialogFragment = new DeviceNameDialogFragment();
            Bundle args = new Bundle();
            args.putString("MAC", deviceMac);
            deviceNameDialogFragment.setArguments(args);
            deviceNameDialogFragment.show(getSupportFragmentManager(), "DeviceNameDialogFragment");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}