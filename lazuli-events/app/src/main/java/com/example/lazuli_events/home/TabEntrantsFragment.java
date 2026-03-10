package com.example.lazuli_events.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.lazuli_events.R;

import java.util.ArrayList;


public class TabEntrantsFragment extends Fragment {

    ListView entrantListView;
    ArrayList<String> entrantList = new ArrayList<>();
    EntrantListAdapter entrantListAdapter;

    // spinner for filtering entrant status
    Spinner statusSpinner;
    Button messageButton;

    public TabEntrantsFragment() {
        // required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_entrants_tab, container, false);

        // create a placeholder list of entrants
        entrantList.add("entrant 1");
        entrantList.add("entrant 2");
        entrantList.add("entrant 3");
        entrantList.add("entrant 4");
        entrantList.add("entrant 5");
        entrantList.add("entrant 6");

        // get list view and set adapter
        entrantListView = rootView.findViewById(R.id.entrant_tab_listView);
        entrantListAdapter = new EntrantListAdapter(rootView.getContext(), entrantList);
        entrantListView.setAdapter(entrantListAdapter);

        // set up the status spinner (Issue #35)
        statusSpinner = rootView.findViewById(R.id.entrant_status_spinner);
        String[] statusOptions = {"Waitlist", "Chosen", "Cancelled"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(rootView.getContext(),
                android.R.layout.simple_spinner_item, statusOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(spinnerAdapter);

        // handle spinner selection changes
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = statusOptions[position];

                // if "Cancelled" is selected, show the drop button (Issue #37)
                if (selectedStatus.equals("Cancelled")) {
                    entrantListAdapter.setShowDropButton(true);
                } else {
                    entrantListAdapter.setShowDropButton(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // set up the message button
        messageButton = rootView.findViewById(R.id.message_entrants_button);
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // placeholder toast
                Toast.makeText(rootView.getContext(), "Opening Message Screen...", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}
