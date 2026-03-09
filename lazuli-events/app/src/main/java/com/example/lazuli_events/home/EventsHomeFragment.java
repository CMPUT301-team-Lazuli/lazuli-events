package com.example.lazuli_events.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lazuli_events.R;
import com.example.lazuli_events.events.EventDetailsFragment;
import com.example.lazuli_events.profile.Profile;
import com.example.lazuli_events.profile.ProfileFragment;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventsHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsHomeFragment extends Fragment {

    TabLayout tabLayout;
    FragmentManager fragmentManager;


    public EventsHomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_events_home, container, false);

        if (savedInstanceState == null) {
            fragmentManager = getActivity().getSupportFragmentManager();
            TabEnteredEventsFragment  homeFragment = new TabEnteredEventsFragment();
            fragmentManager.beginTransaction().replace(R.id.event_fragment_container, homeFragment)
                    .commit();
        }
        // tab navigation
        TabLayout tabLayout = rootView.findViewById(R.id.events_home_tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                String tabText = tab.getText().toString();

                // display the tab's fragment in the container
                switch (tabText) {
                    case "Entered":
                        Log.d("tab", "entered");
                        fragment = new TabEnteredEventsFragment();
                        break;
                    case "Hosting":
                        Log.d("tab", "hosting");
                        fragment = new TabHostedEventsFragment();
                        break;
                }

                if (fragment != null) {
                    fragmentManager.beginTransaction().replace(R.id.event_fragment_container, fragment)
                            .commit();
                } else {
                    Log.d("error", "error");
                }

            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


        return rootView;
    }






    // ignore this test

//    //
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // gets a reference to the nav host activity, to get its navController.
//                // uses the controller to navigate to the user profile fragment, after 1.5 seconds
//                NavGraphActivity navGraphActivity = (NavGraphActivity) getActivity();
//                assert navGraphActivity != null;
//                navGraphActivity.navController.navigate(R.id.action_userEventsFragment_to_userProfileFragment);
//            }
//        }, 1_500);
//    }
}