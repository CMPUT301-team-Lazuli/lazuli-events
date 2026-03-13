package com.example.lazuli_events.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lazuli_events.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class EntrantMapFragment extends Fragment implements OnMapReadyCallback {

    public EntrantMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.entrant_map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLng edmonton = new LatLng(53.5461, -113.4938);
        LatLng entrant1 = new LatLng(53.5444, -113.4909);
        LatLng entrant2 = new LatLng(53.5505, -113.5001);
        LatLng entrant3 = new LatLng(53.5418, -113.4827);

        googleMap.addMarker(new MarkerOptions().position(entrant1).title("Entrant 1"));
        googleMap.addMarker(new MarkerOptions().position(entrant2).title("Entrant 2"));
        googleMap.addMarker(new MarkerOptions().position(entrant3).title("Entrant 3"));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, 11.5f));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
}