package com.example.lazuli_events;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


// display and update user info
public class UserProfileFragment extends Fragment {

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);  // inflate the layout

        // code here

        // get user details from database
        // display details in fragment (get id of each field in fragment_user_profile.xml)

        return rootView;
    }
}