package com.example.mainactivity;

import static androidx.core.text.HtmlCompat.fromHtml;

import android.content.Context;
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
import android.widget.Toolbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mainactivity.Database.AppDatabase;
import com.example.mainactivity.Database.entity.InfoEntity;
import com.example.mainactivity.Database.entity.TipsEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Data;

public class MainActivity extends AppCompatActivity {
    private ListView lvDevices;
    private ArrayAdapter<String> deviceAdapter;
    private ArrayList<String> connectedDevices = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();
        addConnectedDevice("NONE", "EC:E3:34:D1:60:7C");

        loadConnectedDevices();
        checkNewDevice();

        AppDatabase db = AppDatabase.getInstance(this);
        //db.infoDao().deleteAllInfo();
        //db.tipsDao().deleteAllTips();
        populateLocalDatabase();

        Context context = getApplicationContext();
        PeriodicWorkRequest notificationWorkRequest = new PeriodicWorkRequest.Builder(BackgroundRoutine.class, 2, TimeUnit.MINUTES).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "BackgroundNotifications",
                ExistingPeriodicWorkPolicy.KEEP,
                notificationWorkRequest
        );
    }

    //TODO: NOTIFICATIONS

    //TODO: MAKE THE THING NOT CHANGEABLE FROM PORTRAIT TO LANDSCAPE (FIX PROVIDED BY JOHN'S BRANMCH)

    //DONE: CHANGE THE NAME THAT IT DISPLAYS HERE TO SOMETHING ELSE.
    //DONE CHANGE IT TO A NAME THAT YOU CAN GET FROM DATABASE. NOT JUST THE IP AND MAC IT MAKES NO SENSE.
    //DONE MAKE A SMALL ACTIVITY THAT LETS YOU GO CHANGE THE NAME OF THE USER AND SEND IT TO DATABASE. MAYBE MAKE THE SENSOR NAME CHANGEABLE BY PRESSING IT?
    //DONE: LIKE A EDIT TEXT THAT WHEN CONFIRMED, SENDS IT STRAIGHT TO THE DATABASE AS A MODIFICATION

    //TODO: LAST THING IS TO MAKE WIFI RECONNECTION SYSTEM IN THE ARDUINO (RISKY) ALSO MAKE IT DETECT THAT IT IS NOT CONNECTED TO INTERNET ANYMORE
    // EITHER BY SENDING A BLUETOOTH VALUE, OR BY HAVING THE PHONE READ THE SHARED PREFERENCES OF THE ARDUINO??? IDK. WE NEED A WAY TO SEND THIS MESSAGE.
    // MAYBE HAVE THE PHONE SEND A MESSAGE TO THE BLUETOOTH RECEIVER, AND IT SENDS BACK A CONFIRMATION, LIKE A 1 TO 1 COMM
    // LIKE PHONE SENDS BT CONNECTED? AND ARDUINO SAYS EITHER YES/NO. BASED ON ANSWER, WE GO TO THE SPECIFIC ACTIVITY.

    //TODO: START NEW DOUGH SHOULD SEND A USER TO THE FEEDING INSTRUCTIONS, THEN SET THE CURRENT DAY TO 0, AND LET THE USER PRESS START FED.
    //TODO: IT SHOULD SET THE DAY TO 1 AFTER IT'S DONE AND ENABLE THE LID.
    //TODO: MAYBE LOOK INTO HOW THE HEIGHT IS MEASURED ON THE ORIGINAL PROJECT IDEA (WEBLINK)
    private void setupViews() {
        // Initialize views
        lvDevices = findViewById(R.id.lvDevices);
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, connectedDevices);
        lvDevices.setAdapter(deviceAdapter);

        //setup action bar
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            ColorDrawable colorDrawable = new ColorDrawable(getColor(R.color.primary_color));
            actionBar.setBackgroundDrawable(colorDrawable);
            actionBar.setTitle(fromHtml("List of Starters",getColor(R.color.on_primary_color)));
        }


        lvDevices.setOnItemClickListener((parent, view, position, id) -> {
            String deviceInfo = connectedDevices.get(position);
            String deviceIp = deviceInfo.split(" - ")[1]; // Extract IP from display string
            String deviceMac = deviceInfo.split(" - ")[2]; //get the mac address

            Intent intent = new Intent(MainActivity.this, DeviceDataActivity.class);
            intent.putExtra("DEVICE_IP", deviceIp);
            intent.putExtra("DEVICE_MAC", deviceMac);
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
            String newMac = intent.getStringExtra("ESP32_MAC");
            addConnectedDevice(newIp, newMac);
        }
    }

    private void addConnectedDevice(String ip, String Mac) {
        String deviceInfo = "ESP32 Device - " + ip + " - " + Mac;
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
    private void populateLocalDatabase(){
        //initialize database
        AppDatabase db = AppDatabase.getInstance(this);

        //info table
        db.infoDao().insertInfo(new InfoEntity(0, "Error", "There was an error with the device. Progress to the next day to restart the sourdough creation process."));
        db.infoDao().insertInfo(new InfoEntity(10, "Requirements", "-Large mason jar (Greater than 750ml)\r\n-Whole wheat flour\r\n-All purpose or bread flour\r\n-Digital scale\r\n-small rubber spatula"));
        db.infoDao().insertInfo(new InfoEntity(1, "Day 1", "To a glass jar add: \r\n-60g Whole wheat flour\r\n-60g water\r\n-Mix together well\r\nYields approx. 120g starter\r\nRest 24h at 70-75°F/21-24°C"));
        db.infoDao().insertInfo(new InfoEntity(2, "Day 2", "Let rest for 24 hours stirring once or twice to oxygenate the mixture. You may or may not see bubbles. Either way is OK."));
        db.infoDao().insertInfo(new InfoEntity(3, "Day 3", "Discard half (60g)\r\nFeed(add): 60g AP or Bread flour 60g water\r\nYields approx. 180g starter\r\nRest 24 hrs at 70-75°F/21-24°C"));
        db.infoDao().insertInfo(new InfoEntity(4, "Day 4", "Discard half (90g)\r\nFeed (add): 60g AP or Bread flour 60g water\r\nYields approx. 210g starter\r\nRest 24 hrs at 70-75°F/21-24°C"));
        db.infoDao().insertInfo(new InfoEntity(5, "Day 5", "Discard half (105g)\r\nFeed (add): 60g AP or Bread flour 60g water\r\nYields approx. 225g starter\r\nRest 24 hrs at 70-75°F/21-24°C"));
        db.infoDao().insertInfo(new InfoEntity(6, "Day 6", "Discard half (112g)\r\nFeed (add): 60g AP or Bread flour 60 g water\r\nYields approx. 233g starter\r\nRest 24 hrs at 70-75°F/ 21-24°C"));
        db.infoDao().insertInfo(new InfoEntity(7, "Day 7", "Discard half (116g)\r\nFeed (add): 60g AP or Bread flour 60g water\r\nYields approx. 236g starter\r\nRest at 70-75°F/21-24°C until active and bubbling. It should then be ready to use!"));
        db.infoDao().insertInfo(new InfoEntity(8, "Day 8", "On day 7+, up to 6 hours after feeding, your starter might be active. An active starter will double in size and have lots of bubbles on the surface. It’s now ready to use!\r\n\nIf your starter has NOT doubled in size, feed every 8-12 hours (not 24) and continue the same formula: Discard half starter. Feed (Add): 60g flour & 60g water at 70-75°F / 21-24°C. Too runny? Add an additional 1-2 tbs of flour." ));

        //tips table
        db.tipsDao().insertTips(new TipsEntity(1, "default", "60g of flour is around 1/2 cup and 60g of water is 60ml"));
        db.tipsDao().insertTips(new TipsEntity(2, "default", "Use a small spatula to scrape down the sides of the jar to prevent mold"));
        db.tipsDao().insertTips(new TipsEntity(3, "humidityFlag", "A dark liquid might appear on the surface. This liquid is called “hooch” and means that your starter is hungry. It also has a very bad smell. This is normal. Any time you see hooch, it’s best to pour it off before feeding it with fresh flour and water."));
        db.tipsDao().insertTips(new TipsEntity(4, "tempFlag", "Temperature is very important. If it’s too cold, your starter won’t rise and the process will take longer. Try to find a warm, 75F spot."));
        db.tipsDao().insertTips(new TipsEntity(5, "day7", "Do the float test! Feed your starter, wait for it to double in size, and then drop a teaspoon of bubbly starter into a jar of water; if it floats to the top it’s ready to use. "));
        db.tipsDao().insertTips(new TipsEntity(6, "day8", "If your starter is not ready at this point, which is quite common, continue feeding it for 1-2 weeks or more. Be patient! "));
    }

}