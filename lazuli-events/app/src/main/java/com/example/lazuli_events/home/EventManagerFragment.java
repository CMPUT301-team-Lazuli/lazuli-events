package com.example.lazuli_events.home;

import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.lazuli_events.MainActivity;
import com.example.lazuli_events.R;
import com.google.android.material.tabs.TabLayout;

/**
 * This is a Fragment that displays the Event Manager for an event hosted by the user.
 * It has a TabLayout, which switches between sections of the event manager.
 */
public class EventManagerFragment extends Fragment {

    ImageButton newNotifButton;
    TabLayout tabLayout;
    FragmentManager fragmentManager;

    public EventManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_event_manager, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();



        // initialize compose new notification button
        newNotifButton = rootView.findViewById(R.id.compose_notification_button);

        newNotifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.navController.navigate(R.id.composeNotificationFragment);
            }
        });


        // set the first tab of the layout as the default view
        if (savedInstanceState == null) {
            fragmentManager = getActivity().getSupportFragmentManager();
            TabEventStatusFragment  eventStatusFragment = new TabEventStatusFragment(); // c
            fragmentManager.beginTransaction().replace(R.id.event_manager_fragment_container, eventStatusFragment)
                    .commit();
        }

        // handle tab navigation
        tabLayout = rootView.findViewById(R.id.event_manager_tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                String tabText = tab.getText().toString();

                // create the fragment for each tab of the event manager
                switch (tabText) {
                    case "Event Status":
                        fragment = new TabEventStatusFragment();
                        break;
                    case "Entrants":
                        fragment = new TabEntrantsFragment();
                        break;
                    case "View and Edit":
                        fragment = new TabViewAndEditFragment();
                        break;
                }

                // replace the fragment displayed in the tab layout's container
                if (fragment != null) {
                    fragmentManager.beginTransaction().replace(R.id.event_manager_fragment_container, fragment)
                            .commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        // code here

        return rootView;
    }


}