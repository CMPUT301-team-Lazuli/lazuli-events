package com.example.lazuli_events.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.lazuli_events.R;
import com.example.lazuli_events.profile.Profile;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays and manages the list of entrants for an event.
 * Allows the organizer to view the roster, filter entrants by status,
 * select entrants, and remove unresponsive entrants.
 */
public class TabEntrantsFragment extends Fragment {

    private ListView entrantListView;
    private EntrantListAdapter entrantListAdapter;

    private ArrayList<Entrant> masterEntrantList;
    private ArrayList<Entrant> displayedEntrantList;

    private TextView entrantsCountTextView;
    private ImageButton deleteButton;
    private ChipGroup filterChipGroup;
    private CheckBox selectAllCheckbox; // added for select ALL functionality

    /**
     * Required empty public constructor for fragment installment.
     */
    public TabEntrantsFragment() {
    }

    /**
     * Called to have the fragment for its user interface view.
     * Initializes the layout, sets up the list adapter with mock data,
     * and configures event listeners for selection, filtering, and deletion.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being reconstructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_entrants_tab, container, false);

        entrantListView = rootView.findViewById(R.id.entrant_tab_listView);
        entrantsCountTextView = rootView.findViewById(R.id.entrants_count_textView);
        deleteButton = rootView.findViewById(R.id.delete_entrants_button);
        filterChipGroup = rootView.findViewById(R.id.filter_chip_group);
        selectAllCheckbox = rootView.findViewById(R.id.select_all_checkbox);

        masterEntrantList = new ArrayList<>();
        displayedEntrantList = new ArrayList<>();

        Profile p1 = new Profile("entrant1", "entrant1@test.com", "123", "test1", "on", new ArrayList<>());
        Profile p2 = new Profile("entrant2", "entrant2@test.com", "456", "test2", "on", new ArrayList<>());
        Profile p3 = new Profile("entrant3", "entrant3@test.com", "789", "test3", "on", new ArrayList<>());

        masterEntrantList.add(new Entrant(p1, "Accepted"));
        masterEntrantList.add(new Entrant(p2, "Waitlisted"));
        masterEntrantList.add(new Entrant(p3, "Cancelled"));

        displayedEntrantList.addAll(masterEntrantList);

        entrantListAdapter = new EntrantListAdapter(requireContext(), displayedEntrantList);
        entrantListView.setAdapter(entrantListAdapter);
        updateEntrantCount();

        // tap a row to select/deselect it
        entrantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Handles the item click event for the entrant list.
             * Toggles the selection state of the clicked entrant.
             * @param parent The AdapterView where the click happened.
             * @param view The view within the AdapterView that was clicked.
             * @param position The position of the view in the adapter.
             * @param id The row id of the item that was clicked.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entrant clickedEntrant = displayedEntrantList.get(position);
                // flip the selection state (true to false, or false to true)
                clickedEntrant.setSelected(!clickedEntrant.isSelected());
                entrantListAdapter.notifyDataSetChanged(); // redraw list to show grey highlight
            }
        });

        // select all checkbox logic
        selectAllCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Handles the checked state change for the "Select All" checkbox.
             * Selects or deselects all currently displayed entrants.
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked The new checked state of buttonView.
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // apply the checkmark state to everyone currently visible on screen
                for (Entrant entrant : displayedEntrantList) {
                    entrant.setSelected(isChecked);
                }
                entrantListAdapter.notifyDataSetChanged();
            }
        });

        // filter logic
        filterChipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            /**
             * Handles the checked state change for the filter statuses.
             * Filters the displayed list of entrants based on the selected statuses.
             * @param group The ChipGroup whose state has changed.
             * @param checkedIds A list of the currently checked statuses IDs.
             */
            @Override
            public void onCheckedChanged(ChipGroup group, List<Integer> checkedIds) {
                displayedEntrantList.clear();

                if (checkedIds.isEmpty()) {
                    displayedEntrantList.addAll(masterEntrantList);
                } else {
                    for (Entrant entrant : masterEntrantList) {
                        String entrantStatus = entrant.getStatus().toLowerCase();
                        for (int id : checkedIds) {
                            Chip selectedChip = group.findViewById(id);
                            String chipText = selectedChip.getText().toString().toLowerCase();

                            if (entrantStatus.equals(chipText)) {
                                displayedEntrantList.add(entrant);
                                break;
                            }
                        }
                    }
                }

                // clear selections when changing filters so don't accidentally delete hidden people
                for (Entrant e : masterEntrantList) {
                    e.setSelected(false);
                }
                selectAllCheckbox.setChecked(false);

                entrantListAdapter.notifyDataSetChanged();
                updateEntrantCount();
            }
        });

        // delete logic
        deleteButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the click event for the delete button.
             * Removes all selected entrants.
             * @param v The view that was clicked (the delete button).
             */
            @Override
            public void onClick(View v) {
                // gather everyone who is selected
                ArrayList<Entrant> entrantsToDelete = new ArrayList<>();
                for (Entrant entrant : masterEntrantList) {
                    if (entrant.isSelected()) {
                        entrantsToDelete.add(entrant);
                    }
                }

                if (entrantsToDelete.isEmpty()) {
                    Toast.makeText(getContext(), "No entrants selected to delete.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // remove them from our lists
                masterEntrantList.removeAll(entrantsToDelete);
                displayedEntrantList.removeAll(entrantsToDelete);

                // reset select all checkbox and update UI
                selectAllCheckbox.setChecked(false);
                entrantListAdapter.notifyDataSetChanged();
                updateEntrantCount();

                Toast.makeText(getContext(), "Deleted " + entrantsToDelete.size() + " entrants.", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    /**
     * Updates the UI text view to display the current number of entrants.
     */
    private void updateEntrantCount() {
        entrantsCountTextView.setText("Entrants (" + displayedEntrantList.size() + ")");
    }
}