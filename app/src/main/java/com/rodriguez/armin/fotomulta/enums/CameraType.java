package com.rodriguez.armin.fotomulta.enums;

/**
 * Created by armin on 29/08/16.
 */
public enum CameraType {

    TRAFFIC_LIGHT(1),
    VISIBLE(2),
    HIDDEN(3);

    private int cameraId;

    CameraType(int cameraId)
    {
        this.cameraId = cameraId;
    }

    public int getCameraId()
    {
        return cameraId;
    }
}
