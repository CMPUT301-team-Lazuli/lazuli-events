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
 * Fragment that serves as the main home screen for user-related events.
 *
 * <p>This fragment provides a tab-based interface that lets the user switch
 * between:</p>
 * <ul>
 *     <li>Entered events</li>
 *     <li>Hosted events</li>
 * </ul>
 *
 * <p>It also includes a button that navigates to the event creation screen.</p>
 */
public class EventsHomeFragment extends Fragment {

    /** Tab layout used to switch between entered and hosted event views. */
    private TabLayout tabLayout;

    /** Child fragment manager used to swap tab content fragments. */
    private FragmentManager fragmentManager;

    /** Button used to navigate to the create-event screen. */
    private ImageButton btnCreateEvent;

    /**
     * Required empty public constructor.
     */
    public EventsHomeFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the layout, initializes tab navigation, sets the default tab content,
     * and configures the create-event button.
     *
     * @param inflater the LayoutInflater used to inflate views
     * @param container the parent view that the fragment UI should attach to
     * @param savedInstanceState previously saved state, or {@code null} if none exists
     * @return the root view of the fragment
     */
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

            /**
             * Called when a tab is selected.
             *
             * <p>Creates and displays the fragment corresponding to the selected tab.</p>
             *
             * @param tab the selected tab
             */
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

            /**
             * Called when a tab is unselected.
             *
             * @param tab the unselected tab
             */
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            /**
             * Called when the currently selected tab is selected again.
             *
             * @param tab the reselected tab
             */
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return rootView;
    }
}