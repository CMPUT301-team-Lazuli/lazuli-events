package com.example.lazuli_events.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lazuli_events.R;

/**
 * This fragment contains a list of events the User has entered.
 * Clicking into an event navigates to the details page.
 */
public class TabEnteredEventsFragment extends Fragment {



    public TabEnteredEventsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_entered_events, container, false);
    }
}