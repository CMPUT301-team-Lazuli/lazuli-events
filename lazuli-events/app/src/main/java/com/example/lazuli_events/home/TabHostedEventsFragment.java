package com.example.lazuli_events.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lazuli_events.R;

/**
 * This fragment contains a list of events created and hosted by the User.
 * Clicking into an event navigates to event manager page.
 */
public class TabHostedEventsFragment extends Fragment {


    public TabHostedEventsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hosted_events, container, false);
    }
}