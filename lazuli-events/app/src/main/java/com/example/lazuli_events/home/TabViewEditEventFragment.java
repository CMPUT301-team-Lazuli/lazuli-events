package com.example.lazuli_events.home;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lazuli_events.R;
import com.google.android.material.card.MaterialCardView;

/**
 * Fragment that provides options for viewing and editing an event.
 *
 * <p>This fragment displays action cards that allow the organizer to:</p>
 * <ul>
 *     <li>View event details</li>
 *     <li>Edit the event</li>
 *     <li>Open a map showing entrant locations</li>
 *     <li>Toggle whether geolocation is required</li>
 * </ul>
 *
 * <p>Some actions currently show placeholder toast messages and can be expanded
 * later with navigation or Firestore updates.</p>
 */
public class TabViewEditEventFragment extends Fragment {

    /** Card used to open the event details view. */
    private MaterialCardView cardViewEvent;

    /** Card used to open the event edit screen. */
    private MaterialCardView cardEditEvent;

    /** Card used to open the entrant map screen. */
    private MaterialCardView cardViewMap;

    /** Checkbox used to toggle whether geolocation is required for the event. */
    private CheckBox cbGeolocationRequired;

    /**
     * Required empty public constructor.
     */
    public TabViewEditEventFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the fragment layout, binds views, and sets up click listeners
     * for the available event management actions.
     *
     * @param inflater the LayoutInflater used to inflate views
     * @param container the parent view that the fragment UI should attach to
     * @param savedInstanceState previously saved state, or {@code null} if none exists
     * @return the root view of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_edit_event, container, false);

        cardViewEvent = rootView.findViewById(R.id.card_view_event);
        cardEditEvent = rootView.findViewById(R.id.card_edit_event);
        cardViewMap = rootView.findViewById(R.id.card_view_map);
        cbGeolocationRequired = rootView.findViewById(R.id.cb_geolocation_required);

        cardViewMap.setOnClickListener(v -> openEntrantMap());

        cbGeolocationRequired.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked
                    ? "Geolocation requirement enabled"
                    : "Geolocation requirement disabled";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            // TODO later:
            // save this value to Firestore for the current event
        });

        cardViewEvent.setOnClickListener(v ->
                Toast.makeText(requireContext(), "View Event clicked", Toast.LENGTH_SHORT).show()
        );

        cardEditEvent.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Edit Event clicked", Toast.LENGTH_SHORT).show()
        );

        return rootView;
    }

    /**
     * Opens the entrant map screen by replacing the current fragment
     * with an instance of {@link EntrantMapFragment}.
     *
     * <p>The transaction is added to the back stack so the user can return
     * to this fragment using the back button.</p>
     */
    private void openEntrantMap() {
        EntrantMapFragment entrantMapFragment = new EntrantMapFragment();

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.event_manager_fragment_container, entrantMapFragment)
                .addToBackStack(null)
                .commit();
    }
}