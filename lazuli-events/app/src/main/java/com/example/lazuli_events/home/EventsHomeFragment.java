package com.example.lazuli_events.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lazuli_events.R;
import com.google.android.material.tabs.TabLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventsHomeFragment extends Fragment {

    private TabLayout tabLayout;
    private FragmentManager fragmentManager;
    private ImageButton btnCreateEvent;

    public EventsHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_events_home, container, false);

        fragmentManager = getChildFragmentManager();

        tabLayout = rootView.findViewById(R.id.events_home_tabLayout);
        btnCreateEvent = rootView.findViewById(R.id.btnCreateEvent);

        // default tab content
        if (savedInstanceState == null) {
            Fragment homeFragment = new TabEnteredEventsFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.event_fragment_container, homeFragment)
                    .commit();
        }

        // calendar button -> navigate to create event fragment
        btnCreateEvent.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_userEventsFragment_to_tabCreateEventFragment);
        });

        // tab navigation
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                String tabText = tab.getText().toString();

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
                    fragmentManager.beginTransaction()
                            .replace(R.id.event_fragment_container, fragment)
                            .commit();
                } else {
                    Log.d("error", "error switching tabs");
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
}