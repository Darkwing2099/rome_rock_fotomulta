package com.rodriguez.armin.fotomulta.controllers;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.rodriguez.armin.fotomulta.models.MarkerModel;

import java.util.Locale;

/**
 * Created by armin on 3/09/16.
 */
public class Utils {

    /**
     * Convert speed in kilometer/hour or miles/hour
     * @param locationSpeed
     * @return
     */
    public static String convertLocationSpeedToKmHr(double locationSpeed)
    {
        int speed;

        if(Locale.getDefault().getLanguage().equals("es"))
            speed = (int)((locationSpeed*3600)/1000);
        else
            speed = (int) (locationSpeed*2.2369);

        return String.valueOf(speed);
    }

    /**
     * Calculate distances between to points
     * @param from
     * @param to
     * @return distance in meters
     */
    public static String getDistanceBetween(LatLng from, LatLng to)
    {
        float[] results = new float[1];
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results);

        return String.valueOf((int)results[0]);
    }

    /**
     * convert speed limit. Miles to Km or Km to miles according to device language
     * @param context
     * @param markerId
     */
    public static String convertSpeedAccordingToLanguage(Context context, long markerId, int speed)
    {
        String finalSpeed;

        String captureLanguage = MarkerModel.getCaptureLanguage(context, markerId);
        String currentLanguage = Locale.getDefault().getLanguage();

        if(captureLanguage.equals(currentLanguage) && currentLanguage.equals("es") )
            finalSpeed = String.valueOf(speed);
        else
            finalSpeed = String.valueOf(speed * 1.60934);

        return finalSpeed;
    }
}
