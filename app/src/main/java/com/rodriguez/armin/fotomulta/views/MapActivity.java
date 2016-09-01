package com.rodriguez.armin.fotomulta.views;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.rodriguez.armin.fotomulta.R;
import com.rodriguez.armin.fotomulta.beans.Marker;
import com.rodriguez.armin.fotomulta.controllers.GeofenceController;
import com.rodriguez.armin.fotomulta.controllers.MarkerController;
import com.rodriguez.armin.fotomulta.enums.CameraType;
import com.software.shell.fab.ActionButton;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private Context context;

    //to manage map
    private GoogleMap mMap;
    private LatLng selectedLocation;
    private Location currentLocation;

    //to manage marker creation
    private LinearLayout markerInfoForm;
    private EditText markerName, markerSpeedLimit;
    private Button acceptBtn, cancelBtn;
    private Spinner cameraSelector;
    private CameraType cameraType;
    private ActionButton menuButton;
    private int screenWidth, screenHeight;
    private MarkerController markerController;

    //Geofence
    private GoogleApiClient mGoogleApiClient;
    private GeofenceController geofenceController;

    private float currentSpeed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        context = getBaseContext();
        markerController = new MarkerController(context);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            geofenceController = new GeofenceController(context, mGoogleApiClient);
        }

        //get screen size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        markerInfoForm = (LinearLayout) findViewById(R.id.ll_data_container);
        markerName = (EditText) findViewById(R.id.et_name);
        markerSpeedLimit = (EditText) findViewById(R.id.et_speed_limit);
        acceptBtn = (Button) findViewById(R.id.btn_accept);
        cancelBtn = (Button) findViewById(R.id.btn_cancel);

        //spinner configuration
        cameraSelector = (Spinner) findViewById(R.id.sp_camera_type_selector);
        cameraSelector.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.camera_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        cameraSelector.setAdapter(adapter);
        cameraSelector.setSelection(0);

        //Float Action Button configuration
        menuButton = (ActionButton) findViewById(R.id.action_button);
        menuButton.setImageResource(R.drawable.map_marker);
        menuButton.setButtonColor(getResources().getColor(R.color.dodger_blue));
        menuButton.setShadowResponsiveEffectEnabled(true);
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        initFloatButton(itemBuilder);

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = markerName.getText().toString();
                String speedLimit = markerSpeedLimit.getText().toString();

                if (name.isEmpty() || speedLimit.isEmpty() || cameraType == null)
                    Toast.makeText(context, R.string.fill_fields, Toast.LENGTH_SHORT).show();
                else {
                    Marker marker = markerController.createMarker(mMap,name,speedLimit,selectedLocation,cameraType);
                    geofenceController.addGeofence(marker);

                    markerInfoForm.setVisibility(View.GONE);
                    resetForm();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerInfoForm.setVisibility(View.GONE);
                resetForm();
            }
        });

        cameraSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position)
                {
                    case 1:
                        cameraType = CameraType.TRAFFIC_LIGHT;
                        break;
                    case 2:
                        cameraType = CameraType.VISIBLE;
                        break;
                    case 3:
                        cameraType = CameraType.HIDDEN;
                        break;
                }

                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                selectedLocation = latLng;
                showDialog();
            }
        });
    }

    private void showDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setMessage(getResources().getString(R.string.add_marker_question));
        dialog.setCancelable(false);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                selectedLocation = null;
            }
        });

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                markerInfoForm.setVisibility(View.VISIBLE);
            }
        });

        dialog.show();
    }

    /**
     * Set empty form fields
     */
    private void resetForm() {
        markerName.setText("");
        markerSpeedLimit.setText("");
        cameraSelector.setSelection(0);
        cameraType = null;
        hideKeyboard();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        currentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (currentLocation != null) {

            LatLng latLng= new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void hideKeyboard()
    {
        markerName.requestFocus();
        ((InputMethodManager) markerName.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(markerName.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * Set extra buttons to float Action Button
     * @param itemBuilder
     */
    private void initFloatButton(SubActionButton.Builder itemBuilder)
    {
        //show all markers button
        ImageView showAllIcon = new ImageView(this); // Create an icon
        showAllIcon.setImageResource(R.drawable.two_markers);

        SubActionButton showAllMarkersButton = itemBuilder.setContentView(showAllIcon).build();
        showAllMarkersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_blue_light));

        //Hide all markers button
        ImageView hideAllIcon = new ImageView(this); // Create an icon
        hideAllIcon.setImageResource(R.drawable.close_marker);

        SubActionButton hideAllMarkersButton = itemBuilder.setContentView(hideAllIcon).build();
        hideAllMarkersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_blue_light));

        //traffic light camera type button
        ImageView trafficLightIcon = new ImageView(this); // Create an icon
        trafficLightIcon.setImageResource(R.drawable.traffic_light);

        SubActionButton showTrafficLightTypeButton = itemBuilder.setContentView(trafficLightIcon).build();
        showTrafficLightTypeButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_green));

        //visible camera type button
        ImageView visibleIcon = new ImageView(this); // Create an icon
        visibleIcon.setImageResource(R.drawable.open_eye);

        SubActionButton showVisibleTypeButton = itemBuilder.setContentView(visibleIcon).build();
        showVisibleTypeButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_yellow));

        //hidden camera type button
        ImageView hiddenIcon = new ImageView(this); // Create an icon
        hiddenIcon.setImageResource(R.drawable.close_eye);

        SubActionButton showHiddenTypeButton = itemBuilder.setContentView(hiddenIcon).build();
        showHiddenTypeButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_red));


        final FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(showAllMarkersButton)
                .addSubActionView(hideAllMarkersButton)
                .addSubActionView(showTrafficLightTypeButton)
                .addSubActionView(showVisibleTypeButton)
                .addSubActionView(showHiddenTypeButton)
                .attachTo(menuButton)
                .build();

        showAllMarkersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionMenu.close(true);
                mMap.clear();
                markerController.showAllMarkers(mMap, screenWidth, screenHeight);
            }
        });

        hideAllMarkersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionMenu.close(true);
                mMap.clear();

                LatLng latLng= new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
            }
        });

        showTrafficLightTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionMenu.close(true);
                mMap.clear();
                markerController.showFilteringMarkers(mMap, CameraType.TRAFFIC_LIGHT, screenWidth, screenHeight);
            }
        });

        showVisibleTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionMenu.close(true);
                mMap.clear();
                markerController.showFilteringMarkers(mMap, CameraType.VISIBLE, screenWidth, screenHeight);
            }
        });

        showHiddenTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionMenu.close(true);
                mMap.clear();
                markerController.showFilteringMarkers(mMap, CameraType.HIDDEN, screenWidth, screenHeight);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        currentSpeed = location.getSpeed();

        Toast.makeText(context,"velocidad actual: " + currentSpeed, Toast.LENGTH_SHORT).show();
        Log.d("armin", "location change");

    }
}
