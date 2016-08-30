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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.rodriguez.armin.fotomulta.R;
import com.rodriguez.armin.fotomulta.beans.Marker;
import com.rodriguez.armin.fotomulta.controllers.MarkerController;
import com.rodriguez.armin.fotomulta.enums.CameraType;
import com.rodriguez.armin.fotomulta.models.MarkerModel;
import com.software.shell.fab.ActionButton;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private LinearLayout markerInfoForm;
    private EditText markerName, markerSpeedLimit;
    private Button acceptBtn, cancelBtn;
    private LatLng selectedLocation;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Spinner cameraSelector;
    private CameraType cameraType;
    private Context context;
    private MarkerController markerController;
    private ActionButton menuButton;

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
        }

        markerInfoForm = (LinearLayout) findViewById(R.id.ll_data_container);
        markerName = (EditText) findViewById(R.id.et_name);
        markerSpeedLimit = (EditText) findViewById(R.id.et_speed_limit);
        acceptBtn = (Button) findViewById(R.id.btn_accept);
        cancelBtn = (Button) findViewById(R.id.btn_cancel);
        cameraSelector = (Spinner) findViewById(R.id.sp_camera_type_selector);
        cameraSelector.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        menuButton = (ActionButton) findViewById(R.id.action_button);

        menuButton.setImageResource(R.drawable.menu_dots_icon);
        menuButton.setButtonColor(getResources().getColor(R.color.red));
        menuButton.setShadowResponsiveEffectEnabled(true);

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);

        initFloatButton(itemBuilder);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.camera_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        cameraSelector.setAdapter(adapter);
        cameraSelector.setSelection(0);

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = markerName.getText().toString();
                String speedLimit = markerSpeedLimit.getText().toString();

                if (name.isEmpty() || speedLimit.isEmpty() || cameraType == null)
                    Toast.makeText(context, R.string.fill_fields, Toast.LENGTH_SHORT).show();
                else {
                    Marker marker = new Marker();
                    marker.setName(name);
                    marker.setSpeedLimit(Integer.parseInt(speedLimit));
                    marker.setLatLng(selectedLocation);
                    marker.setOwn(true);
                    marker.setCameraType(cameraType);

                    String speedLimitPhrase = getResources().getString(R.string.speed_limit) + speedLimit + getResources().getString(R.string.mph);

                    mMap.addMarker(new MarkerOptions().position(selectedLocation).title(name).snippet(speedLimitPhrase));
                    markerInfoForm.setVisibility(View.GONE);

                    MarkerModel.saveMarker(context, marker);
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

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        markerController.showAllMarkers(mMap, width, height);

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

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {

            LatLng latLng= new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
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
        ImageView trafficLightIcon = new ImageView(this); // Create an icon
        trafficLightIcon.setImageResource(R.drawable.traffic_light_icon);

        SubActionButton trafficLightButton = itemBuilder.setContentView(trafficLightIcon).build();
        trafficLightButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.round));

        ImageView visibleIcon = new ImageView(this); // Create an icon
        visibleIcon.setImageResource(R.drawable.open_eye_icon);

        SubActionButton visibleButton = itemBuilder.setContentView(visibleIcon).build();
        visibleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.round));

        ImageView hiddenIcon = new ImageView(this); // Create an icon
        hiddenIcon.setImageResource(R.drawable.close_eye_icon);

        SubActionButton hiddenButton = itemBuilder.setContentView(hiddenIcon).build();
        hiddenButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.round));

        final FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(trafficLightButton)
                .addSubActionView(visibleButton)
                .addSubActionView(hiddenButton)
                .attachTo(menuButton)
                .build();
    }

}
