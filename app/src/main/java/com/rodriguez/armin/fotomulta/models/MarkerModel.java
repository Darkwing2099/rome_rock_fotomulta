package com.rodriguez.armin.fotomulta.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;
import com.rodriguez.armin.fotomulta.beans.Marker;
import com.rodriguez.armin.fotomulta.enums.CameraType;

import java.util.ArrayList;

/**
 * Manage Marker data
 * Created by armin on 29/08/16.
 *
 */
public class MarkerModel {

    public static String TABLE_NAME = "marker";
    public static String COLUMN_ID = "_id";
    public static String COLUMN_NAME = "name";
    public static String COLUMN_LONGITUDE = "longitude";
    public static String COLUMN_LATITUDE = "latitude";
    public static String COLUMN_SPEED_LIMIT = "speed_limit";
    public static String COLUMN_IS_OWN = "is_own";
    public static String COLUMN_CAMERA_TYPE = "camera_type";

    /**
     * Return a string with a query to create a new marker data table
     * @return
     */
    public static String getCreateTableQuery()
    {
        return "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME + " TEXT, "+ COLUMN_LONGITUDE + " INTEGER, " + COLUMN_LATITUDE + " INTEGER, " +
                COLUMN_SPEED_LIMIT + " INTEGER, " + COLUMN_IS_OWN + " INTEGER, " + COLUMN_CAMERA_TYPE + " INTEGER)";
    }

    /**
     * Save a Maker instances in SQLite database
     * @param context
     * @param marker
     * @return
     */
    public static boolean saveMarker(Context context, Marker marker)
    {
        boolean response = false;

        DBManager dbManager = new DBManager(context);
        SQLiteDatabase db = dbManager.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, marker.getName());
        values.put(COLUMN_LONGITUDE, marker.getLatLng().longitude);
        values.put(COLUMN_LATITUDE, marker.getLatLng().latitude);
        values.put(COLUMN_SPEED_LIMIT, marker.getSpeedLimit());
        values.put(COLUMN_IS_OWN, marker.isOwn() ? 1 : 0);
        values.put(COLUMN_CAMERA_TYPE, marker.getCameraType().getCameraId());

        if(db.insert(TABLE_NAME, null, values) != -1)
            response = true;

        db.close();

        return response;
    }

    /**
     * Get all markers
     * @param context
     * @return Marker Array list if exists, null otherwise
     */
    public static ArrayList<Marker> getAllMarkers(Context context)
    {
        ArrayList<Marker> markerList = null;

        DBManager dbManager = new DBManager(context);
        SQLiteDatabase db = dbManager.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        if(cursor.moveToFirst())
        {
            markerList = new ArrayList<>();

            do {
                Marker marker = new Marker();

                marker.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));

                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE));
                marker.setLatLng(new LatLng(latitude,longitude));

                marker.setSpeedLimit(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SPEED_LIMIT)));

                int isOwn = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_OWN));
                marker.setOwn(isOwn== 1 ? true : false);

                int cameraType = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAMERA_TYPE));
                marker.setCameraType(cameraType == 1 ? CameraType.TRAFFIC_LIGHT : cameraType == 2 ? CameraType.VISIBLE : CameraType.HIDDEN);

                markerList.add(marker);

            }while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return markerList;
    }

}
