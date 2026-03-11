package com.example.lazuli_events.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.lazuli_events.R;
import com.example.lazuli_events.model.Event;

public class TabEventStatusFragment extends Fragment {

    public TabEventStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_event_status, container, false);

        // after you load your event from Firebase:
        // bindRegistrationStatus(event);

        return rootView;
    }

    private void bindRegistrationStatus(Event event) {
        long now = System.currentTimeMillis();

        String windowText;
        if (event.getRegistrationStartMillis() == null || event.getRegistrationEndMillis() == null) {
            windowText = "Registration period not set";
        } else if (now < event.getRegistrationStartMillis()) {
            windowText = "Opens: " + formatDateTime(event.getRegistrationStartMillis());
        } else if (now <= event.getRegistrationEndMillis()) {
            windowText = "Open until: " + formatDateTime(event.getRegistrationEndMillis());
        } else {
            windowText = "Closed: " + formatDateTime(event.getRegistrationEndMillis());
        }

        // set text here
    }

    private String formatDateTime(long millis) {
        return String.valueOf(millis);
    }
}