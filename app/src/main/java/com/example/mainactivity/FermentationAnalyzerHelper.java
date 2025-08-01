package com.example.mainactivity;

import com.google.firebase.logger.Logger;

public class FermentationAnalyzerHelper {

    /**
     * Helper class which is used to interpret the sensors data
     */

    public static String analyzeTemperature(float temp) {
        /**
         * Function to give a status on sourdough based on temperature. The temperature will vary widely through the process.
         * Furthermore, there are many external factors that effect the the sourdough being complete. The most useful info
         * from the temperature is whether or not it is being fermented at best range to have best flavour.
         */
        String tempAdvice;
        if(temp < 24)
            tempAdvice = "Temperature low: Fermentation may be slow.";
        else if(temp > 28)
            tempAdvice = "Temperature high: risk of off-flavours of sourdough";
        else
            tempAdvice = "Temperature optimal for fermentation.";

        return tempAdvice;
    }

    public static String analyzeHumidity(float humidity) {
        String humidityAdvice;
        /**
         * Found this which talks about humidity affects on sourdough : https://sourdoughgeek.com/does-humidity-affect-sourdough/
         */
        if(humidity < 70)
            humidityAdvice = "Humidity is low, dough may dry out. Consider adding water.";
        else if(humidity <= 75)
            humidityAdvice = "Humidity is optimal for sourdough fermentation.";
        else if(humidity <= 85)
            humidityAdvice = "Humidity is high. Watch dough closely.";
        else
            humidityAdvice = "Very high humidity. Dough may over-ferment or dry out quickly. Consider reducing the hydration.";

        return humidityAdvice;
    }

    public static String analyzeToF(float currentToF, float initialToF) {
        /**
         * Function to look at change in volume to determine whether the starter is finished or not
         * gives a message based on the sourdough height (can be used to print messages based on daily trends in sourdough algorithm)
         */

        String ToFAdvice;
        float jarHeight = 195;

        // Convert ToF distances to actual sourdough height in jar
        float currentHeight = jarHeight - currentToF;
        float initialHeight = jarHeight - initialToF;

        // Can check it with volume but the jar isn't uniform in diameter and changes slightly through the jar so might be less accurate
        // float jarDiameter = 80; // goes from about 75 to 85 so take average
        // float radius = jarDiameter / 2.0f;
        // double currentVolume = Math.PI * radius * radius * currentHeight;
        // double initialVolume = Math.PI * radius * radius * initialHeight;
        // double growthFactor = currentVolume / initialVolume;

        // Height-based growth factor
        float growthFactor = currentHeight / initialHeight;

        if (initialHeight <= 0 || currentHeight < 0) {
            ToFAdvice = "Invalid readings.";
            return ToFAdvice;
        }

        if (growthFactor < 1.2f) {
            ToFAdvice = "Starter hasn’t grown much. Consider checking the temperature or feeding starter.";
        } else if (growthFactor < 2.0f) {
            ToFAdvice = "Starter is growing !";
        } else {
            ToFAdvice = "Your starter has doubled (or more) in size!";
        }

        return ToFAdvice;
    }


    public static String analyzeCO2(float co2) {
        String co2Stuff;
        /**
         *
         */
        if(co2 >= 400 && co2 < 1000)
            co2Stuff = "Ambient CO₂ levels—not significant fermentation activity.";
        else if(co2 <= 3000)
            co2Stuff = "Mild fermentation—starter is active.";
        else if(co2 <= 8000)
            co2Stuff = "Strong fermentation—well-fed and vigorous starter or levain.";
        else
            co2Stuff = "Very high buildup—possible jar pressure concerns (especially in sealed systems) and nearing full fermentation.";

        return co2Stuff;
    }

}
