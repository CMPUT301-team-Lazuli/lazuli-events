package com.example.lazuli_events.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.lazuli_events.MainActivity;
import com.example.lazuli_events.R;
import com.google.android.material.tabs.TabLayout;

/**
 * Fragment that displays the event manager interface for an event hosted by the user.
 *
 * <p>This fragment contains a {@link TabLayout} used to switch between different
 * sections of event management, such as:</p>
 * <ul>
 *     <li>Event Status</li>
 *     <li>Entrants</li>
 *     <li>View and Edit</li>
 * </ul>
 *
 * <p>It also includes a button for navigating to the notification composition screen.</p>
 */
public class EventManagerFragment extends Fragment {

    /** Button used to navigate to the compose notification screen. */
    private ImageButton newNotifButton;

    /** Tab layout used for switching between event manager sections. */
    private TabLayout tabLayout;

    /** Child fragment manager used to swap fragments inside the tab container. */
    private FragmentManager fragmentManager;

    /**
     * Required empty public constructor.
     */
    public EventManagerFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the event manager layout, initializes the notification button,
     * sets the default tab fragment, and configures tab navigation behavior.
     *
     * @param inflater the LayoutInflater used to inflate views
     * @param container the parent view that the fragment UI should attach to
     * @param savedInstanceState previously saved state, or {@code null} if none exists
     * @return the root view of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_manager, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();

        fragmentManager = getChildFragmentManager();

        // initialize compose new notification button
        newNotifButton = rootView.findViewById(R.id.compose_notification_button);
        newNotifButton.setOnClickListener(v -> {
            if (mainActivity != null) {
                mainActivity.navController.navigate(R.id.composeNotificationFragment);
            }
        });

        // set the first tab of the layout as the default view
        if (savedInstanceState == null) {
            Fragment eventStatusFragment = new TabEventStatusFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.event_manager_fragment_container, eventStatusFragment)
                    .commit();
        }

        // handle tab navigation
        tabLayout = rootView.findViewById(R.id.event_manager_tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            /**
             * Called when a tab is selected.
             *
             * <p>Creates and displays the corresponding fragment based on the
             * selected tab's text.</p>
             *
             * @param tab the selected tab
             */
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                String tabText = String.valueOf(tab.getText());

                // create the fragment for each tab of the event manager
                switch (tabText) {
                    case "Event Status":
                        fragment = new TabEventStatusFragment();
                        break;

                    case "Entrants":
                        fragment = new TabEntrantsFragment();
                        break;

                    case "View and Edit":
                        fragment = new TabViewEditEventFragment();
                        break;
                }

                // replace the fragment displayed in the tab layout's container
                if (fragment != null) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.event_manager_fragment_container, fragment)
                            .commit();
                }
            }

            /**
             * Called when a tab is no longer selected.
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