package com.rodriguez.armin.fotomulta.views;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.rodriguez.armin.fotomulta.R;
import com.rodriguez.armin.fotomulta.beans.Marker;
import com.rodriguez.armin.fotomulta.controllers.Utils;
import com.rodriguez.armin.fotomulta.interfaces.MapCallback;
import com.rodriguez.armin.fotomulta.interfaces.SpeedInfoCallback;
import com.rodriguez.armin.fotomulta.models.MarkerModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SpeedAlertInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SpeedAlertInfoFragment extends Fragment implements MapCallback{

    private static final String PARAM_MARKER_ID = "markerId";
    private Context context;

    private SpeedInfoCallback speedInfoCallback;

    private ImageButton closeButton;
    private TextView distanceIndicator;
    private TextView speedIndicator;
    private TextView markerSpeedLimit;
    private long markerId;
    private Marker marker;

    public SpeedAlertInfoFragment() {
        // Required empty public constructor
    }

    public static SpeedAlertInfoFragment newInstance(SpeedInfoCallback callback, long markerId) {
        SpeedAlertInfoFragment fragment = new SpeedAlertInfoFragment();
        fragment.setSpeedInfoCallback(callback);
        Bundle extras = new Bundle();
        extras.putLong(PARAM_MARKER_ID, markerId);
        fragment.setArguments(extras);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity().getBaseContext();

        Bundle extras = getArguments();
        if(extras != null)
        {
            markerId = extras.getLong(PARAM_MARKER_ID);
            marker = MarkerModel.getMarkerById(context,markerId);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.speed_alert_info, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        closeButton = (ImageButton) view.findViewById(R.id.btn_close);
        distanceIndicator = (TextView) view.findViewById(R.id.tv_approach_meters);
        markerSpeedLimit = (TextView) view.findViewById(R.id.tv_marker_speed_limit);
        speedIndicator = (TextView) view.findViewById(R.id.tv_current_speed);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        distanceIndicator.setText(Utils.convertSpeedAccordingToLanguage(context, markerId, marker.getSpeedLimit()));

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedInfoCallback.closeWindow();
            }
        });
    }

    public void setSpeedInfoCallback(SpeedInfoCallback speedInfoCallback) {
        this.speedInfoCallback = speedInfoCallback;
    }

    @Override
    public void onSpeedChange(String currentSpeed, String distance) {

        distanceIndicator.setText(distance);
        markerSpeedLimit.setText(String.valueOf(marker.getSpeedLimit()));
        speedIndicator.setText(currentSpeed);

    }
}
