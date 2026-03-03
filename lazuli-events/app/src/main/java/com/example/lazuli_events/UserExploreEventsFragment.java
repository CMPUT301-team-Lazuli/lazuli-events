package com.example.lazuli_events;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


// display cards and handle event sorting
public class UserExploreEventsFragment extends Fragment {

    ListView cardListView;
    ExploreEventsCardAdapter cardAdapter;

    public UserExploreEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate layout
        View rootView = inflater.inflate(R.layout.fragment_user_explore_events, container, false);
        // get ref to host
        NavGraphActivity navGraphActivity = (NavGraphActivity) getActivity();


        // Display list of event cards

        // temp skeleton list data
        ArrayList<String> cardDataList = new ArrayList<>();
        cardDataList.add("event 1");
        cardDataList.add("event 2");

        // get list view and set adapter
        cardListView = rootView.findViewById(R.id.explore_events_listView);
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
                assert navGraphActivity != null;
                navGraphActivity.navController.navigate(R.id.action_userExploreEventsFragment_to_eventDetailsFragment);
            }
        });


        return rootView;
    }
}