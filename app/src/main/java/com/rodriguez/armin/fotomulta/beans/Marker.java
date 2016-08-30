package com.rodriguez.armin.fotomulta.beans;

import com.google.android.gms.maps.model.LatLng;
import com.rodriguez.armin.fotomulta.enums.CameraType;

/**
 * Marker object
 *
 * Created by armin on 29/08/16.
 */
public class Marker {

    private int id;
    private String name;
    private LatLng latLng;
    private int speedLimit;
    private boolean isOwn;
    private CameraType cameraType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public int getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(int speedLimit) {
        this.speedLimit = speedLimit;
    }

    public boolean isOwn() {
        return isOwn;
    }

    public void setOwn(boolean own) {
        isOwn = own;
    }

    public CameraType getCameraType() {
        return cameraType;
    }

    public void setCameraType(CameraType cameraType) {
        this.cameraType = cameraType;
    }
}
