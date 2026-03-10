package com.example.lazuli_events.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.lazuli_events.MainActivity;
import com.example.lazuli_events.R;
import com.example.lazuli_events.events.ExploreEventsCardAdapter;

import java.util.ArrayList;

/**
 * This fragment contains a list of events created and hosted by the User.
 * Clicking into an event navigates to event manager page.
 */
public class TabHostedEventsFragment extends Fragment {

    ListView cardListView;
    ArrayAdapter cardAdapter;

    public TabHostedEventsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_hosted_events, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();

        // Display list of event cards

        // temp skeleton list data
        ArrayList<String> cardDataList = new ArrayList<>();
        cardDataList.add("event 1");
        cardDataList.add("event 2");
        cardDataList.add("event 3");
        cardDataList.add("event 4");
        cardDataList.add("event 5");

        // get list view and set adapter
        cardListView = rootView.findViewById(R.id.event_container_listView);
        cardAdapter = new ExploreEventsCardAdapter(rootView.getContext(), cardDataList);  // fragments use rootVew.getContext instead of "this"
        cardListView.setAdapter(cardAdapter);

        Log.d("test", "test");

        // set card list listeners
        cardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String card = cardDataList.get(position);
                String msg = "clicked";
                Log.d("clicked", msg);

                // navigate to the event details page
                assert mainActivity != null;
                mainActivity.navController.navigate(R.id.eventManagerFragment);
            }
        });

        return rootView;
    }
}