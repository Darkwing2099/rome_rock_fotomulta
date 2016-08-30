package com.rodriguez.armin.fotomulta.models;

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
        return "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY, " + COLUMN_NAME + " TEXT, "+ COLUMN_LONGITUDE + " INTEGER, " + COLUMN_LATITUDE + " INTEGER, " +
                COLUMN_SPEED_LIMIT + " INTEGER, " + COLUMN_IS_OWN + " INTEGER, " + COLUMN_CAMERA_TYPE + " INTEGER)";
    }

}
