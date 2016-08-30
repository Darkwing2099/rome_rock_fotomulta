package com.rodriguez.armin.fotomulta.controllers;

import android.content.Context;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rodriguez.armin.fotomulta.R;
import com.rodriguez.armin.fotomulta.beans.Marker;
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

    public void showAllMarkers(GoogleMap mMap,int width,int height)
    {
        ArrayList<Marker> markerList = MarkerModel.getAllMarkers(context);

        if(!markerList.isEmpty())
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (Marker marker : markerList)
            {
                String speedLimitPhrase = context.getResources().getString(R.string.speed_limit) + marker.getSpeedLimit() + context.getResources().getString(R.string.mph);

                mMap.addMarker(new MarkerOptions().position(marker.getLatLng()).title(marker.getName()).snippet(speedLimitPhrase));
                builder.include(marker.getLatLng());
            }

            LatLngBounds bounds = builder.build();

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,width,height,200);

            mMap.moveCamera(cu);
        }

    }

}
