package com.example.lazuli_events.home;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lazuli_events.R;
import com.google.android.material.card.MaterialCardView;

public class TabViewEditEventFragment extends Fragment {

    private MaterialCardView cardViewEvent;
    private MaterialCardView cardEditEvent;
    private MaterialCardView cardViewMap;
    private CheckBox cbGeolocationRequired;

    public TabViewEditEventFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_edit_event, container, false);

        cardViewEvent = rootView.findViewById(R.id.card_view_event);
        cardEditEvent = rootView.findViewById(R.id.card_edit_event);
        cardViewMap = rootView.findViewById(R.id.card_view_map);
        cbGeolocationRequired = rootView.findViewById(R.id.cb_geolocation_required);

        cardViewMap.setOnClickListener(v -> openEntrantMap());

        cbGeolocationRequired.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked
                    ? "Geolocation requirement enabled"
                    : "Geolocation requirement disabled";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            // TODO later:
            // save this value to Firestore for the current event
        });

        cardViewEvent.setOnClickListener(v ->
                Toast.makeText(requireContext(), "View Event clicked", Toast.LENGTH_SHORT).show()
        );

        cardEditEvent.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Edit Event clicked", Toast.LENGTH_SHORT).show()
        );

        return rootView;
    }

    private void openEntrantMap() {
        EntrantMapFragment entrantMapFragment = new EntrantMapFragment();

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.event_manager_fragment_container, entrantMapFragment)
                .addToBackStack(null)
                .commit();
    }
}