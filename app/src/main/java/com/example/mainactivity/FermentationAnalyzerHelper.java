package com.example.mainactivity;

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
        String msg;
        if(temp < 24)
            msg = "Temperature low: Fermentation may be slow.";
        else if(temp > 28)
            msg = "Temperature high: risk of off-flavours of sourdough";
        else
            msg = "Temperature optimal for fermentation.";

        return msg;
    }

    public static void analyzeHumidity(float humidity) {
        /**
         * https://sourdoughgeek.com/does-humidity-affect-sourdough/?utm_source
         */
    }


}
