package com.rodriguez.armin.fotomulta.controllers;

import android.content.Context;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rodriguez.armin.fotomulta.R;
import com.rodriguez.armin.fotomulta.beans.Marker;
import com.rodriguez.armin.fotomulta.enums.CameraType;
import com.rodriguez.armin.fotomulta.models.MarkerModel;

import java.util.ArrayList;

/**
 * Created by armin on 30/08/16.
 */
public class MarkerController {

    private Context context;

    public MarkerController(Context context)
    {
        this.context = context;
    }

    /**
     * Add marker on map and save in database
     */
    public Marker createMarker(GoogleMap mMap, String name, String speedLimit, LatLng latLng, CameraType cameraType)
    {
        String speedLimitPhrase = context.getResources().getString(R.string.speed_limit) + speedLimit + context.getResources().getString(R.string.mph);

        //add marker to map
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(name)
                .snippet(speedLimitPhrase)
                .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(cameraType))));

        //create marker object
        Marker marker = new Marker();
        marker.setName(name);
        marker.setSpeedLimit(Integer.parseInt(speedLimit));
        marker.setLatLng(latLng);
        marker.setOwn(true);
        marker.setCameraType(cameraType);

        //save marker in database
        MarkerModel.saveMarker(context, marker);

        return marker;
    }

    /**
     * Show all markers type
     * @param mMap
     * @param width
     * @param height
     */
    public void showAllMarkers(GoogleMap mMap,int width,int height)
    {
        ArrayList<Marker> markerList = MarkerModel.getAllMarkers(context);

        if(!markerList.isEmpty())
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (Marker marker : markerList)
            {
                String speedLimitPhrase = context.getResources().getString(R.string.speed_limit) + marker.getSpeedLimit() + context.getResources().getString(R.string.mph);

                mMap.addMarker(new MarkerOptions()
                        .position(marker.getLatLng())
                        .title(marker.getName())
                        .snippet(speedLimitPhrase)
                        .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(marker.getCameraType()))));

                builder.include(marker.getLatLng());
            }

            LatLngBounds bounds = builder.build();

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,width,height,100);

            mMap.animateCamera(cu);
        }
    }

    /**
     * Show only a type of marker depending the camera type
     * @param mMap
     * @param cameraType
     * @param width
     * @param height
     */
    public void showFilteringMarkers(GoogleMap mMap, CameraType cameraType, int width, int height)
    {
        ArrayList<Marker> markerList = MarkerModel.getFilteringMarkers(context, cameraType);

        if(!markerList.isEmpty())
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (Marker marker : markerList)
            {
                String speedLimitPhrase = context.getResources().getString(R.string.speed_limit) + marker.getSpeedLimit() + context.getResources().getString(R.string.mph);

                mMap.addMarker(new MarkerOptions()
                        .position(marker.getLatLng())
                        .title(marker.getName())
                        .snippet(speedLimitPhrase)
                        .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(marker.getCameraType()))));

                builder.include(marker.getLatLng());
            }

            LatLngBounds bounds = builder.build();

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,width,height,100);

            mMap.animateCamera(cu);
        }
    }

    /**
     * Retrieve color depending of camera type
     * @return
     */
    private float getMarkerColor(CameraType cameraType)
    {
        float markerColor = 0;
        switch (cameraType)
        {
            case TRAFFIC_LIGHT:
                markerColor = BitmapDescriptorFactory.HUE_GREEN;
                break;
            case VISIBLE:
                markerColor = BitmapDescriptorFactory.HUE_YELLOW;
                break;
            case HIDDEN:
                markerColor = BitmapDescriptorFactory.HUE_RED;
                break;
        }

        return markerColor;
    }



}
