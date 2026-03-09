package com.example.lazuli_events.notifications;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lazuli_events.R;

/**
 * This is a Fragment that displays setting options for a user's notification preferences.
 * Editing preferences updates the profile in database (?)
 */
public class NotificationSettingsFragment extends Fragment {


    public NotificationSettingsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification_settings, container, false);
    }
}