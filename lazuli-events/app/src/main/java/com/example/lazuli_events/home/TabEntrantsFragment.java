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

public class TabEntrantsFragment extends Fragment {

    private ListView entrantListView;
    private EntrantListAdapter entrantListAdapter;

    private ArrayList<Entrant> masterEntrantList;
    private ArrayList<Entrant> displayedEntrantList;

    private TextView entrantsCountTextView;
    private ImageButton deleteButton;
    private ChipGroup filterChipGroup;
    private CheckBox selectAllCheckbox; // added for select ALL functionality

    public TabEntrantsFragment() {
        // required empty public constructor
    }

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

        Profile p1 = new Profile("Alice", "alice@test.com", "123", "dev1", "on", new ArrayList<>());
        Profile p2 = new Profile("Bob", "bob@test.com", "456", "dev2", "on", new ArrayList<>());
        Profile p3 = new Profile("Charlie", "charlie@test.com", "789", "dev3", "on", new ArrayList<>());

        masterEntrantList.add(new Entrant(p1, "Accepted"));
        masterEntrantList.add(new Entrant(p2, "Waitlisted"));
        masterEntrantList.add(new Entrant(p3, "Cancelled"));

        displayedEntrantList.addAll(masterEntrantList);

        entrantListAdapter = new EntrantListAdapter(requireContext(), displayedEntrantList);
        entrantListView.setAdapter(entrantListAdapter);
        updateEntrantCount();

        // tap a row to select/deselect it
        entrantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

    private void updateEntrantCount() {
        entrantsCountTextView.setText("Entrants (" + displayedEntrantList.size() + ")");
    }
}