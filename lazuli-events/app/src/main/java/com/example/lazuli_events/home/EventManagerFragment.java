package com.example.lazuli_events.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lazuli_events.R;

/**
 * This is a Fragment that displays the Event Manager for an event hosted by the user.
 * It has a TabLayout, which switches between sections of the event manager.
 */
public class EventManagerFragment extends Fragment {

    public EventManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_event_manager, container, false);

        // code here

        return rootView;
    }


}