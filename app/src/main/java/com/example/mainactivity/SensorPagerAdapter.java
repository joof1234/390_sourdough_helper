package com.example.mainactivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SensorPagerAdapter extends FragmentStateAdapter {

    private final String deviceMac;
    private final String[] tabTitles = {"Temperature", "CO₂", "TVOC", "Humidity"};

    public SensorPagerAdapter(FragmentActivity fa, String deviceMac) {
        super(fa);
        //this class is the one to handle the different charts you can display. Not the chart itself
        //only the one who controls which chart is shown.
        //we are passed the mac device so that the actual chart can grab data.
        this.deviceMac = deviceMac;
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            //each chart has a number we decide, here is a layout.
            //this will create a new instance of charts each time you scroll to a new position.
            case 0: return SensorFragment.newInstance(deviceMac, "sht31_temp", "°C");
            case 1: return SensorFragment.newInstance(deviceMac, "co2", "ppm");
            case 2: return SensorFragment.newInstance(deviceMac, "tvoc", "ppb");
            case 3: return SensorFragment.newInstance(deviceMac, "sht31_humidity", "%");
            default: return null;
        }
    }

    @Override
    public int getItemCount() {
        return tabTitles.length;
    }
    //used to get the naming for the tabs
    public String getTabTitle(int position) {
        return tabTitles[position];
    }

}