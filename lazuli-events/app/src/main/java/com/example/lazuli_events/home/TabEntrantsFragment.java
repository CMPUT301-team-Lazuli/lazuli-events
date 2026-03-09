package com.example.lazuli_events.home;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.lazuli_events.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabEntrantsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabEntrantsFragment extends Fragment {

    ListView entrantListView;
    ArrayList<String> entrantList = new ArrayList<>();
    EntrantListAdapter entrantListAdapter;

    public TabEntrantsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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


        return rootView;
    }
}