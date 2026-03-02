package com.example.lazuli_events;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserEventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserEventsFragment extends Fragment {

    public UserEventsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_events, container, false);
    }


    //
    @Override
    public void onResume() {
        super.onResume();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // gets a reference to the nav host activity, to get its navController.
                // uses the controller to navigate to the user profile fragment, after 1.5 seconds
                NavGraphActivity navGraphActivity = (NavGraphActivity) getActivity();
                assert navGraphActivity != null;
                navGraphActivity.navController.navigate(R.id.action_userEventsFragment_to_userProfileFragment);
            }
        }, 1_500);
    }
}