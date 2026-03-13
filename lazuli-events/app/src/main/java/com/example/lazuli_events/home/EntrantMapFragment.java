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

/**
 * Fragment that displays a Google Map showing sample entrant locations.
 *
 * <p>This fragment loads a {@link SupportMapFragment} from its layout and waits for
 * the Google Map to be ready. Once the map is available, it places several markers
 * representing entrant locations and centers the camera on Edmonton.</p>
 *
 * <p>Current marker data is hardcoded for demonstration purposes.</p>
 */
public class EntrantMapFragment extends Fragment implements OnMapReadyCallback {

    /**
     * Required empty public constructor for the fragment manager.
     */
    public EntrantMapFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the fragment layout containing the embedded map fragment.
     *
     * @param inflater the LayoutInflater object that can be used to inflate views
     * @param container the parent view that the fragment UI should attach to
     * @param savedInstanceState previously saved state, or null if none exists
     * @return the root view for this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_map, container, false);
    }

    /**
     * Called after the fragment's view has been created.
     *
     * <p>This method finds the child {@link SupportMapFragment} inside the layout
     * and registers this fragment as the asynchronous callback for map readiness.</p>
     *
     * @param view the fragment's root view
     * @param savedInstanceState previously saved state, or null if none exists
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.entrant_map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Called when the Google Map is ready to be used.
     *
     * <p>This method adds markers for three sample entrant locations, moves the camera
     * to Edmonton, and enables zoom controls for user interaction.</p>
     *
     * @param googleMap the ready-to-use Google Map instance
     */
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