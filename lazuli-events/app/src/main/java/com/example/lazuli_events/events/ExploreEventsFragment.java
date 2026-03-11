package com.example.lazuli_events.events;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.lazuli_events.MainActivity;
import com.example.lazuli_events.R;

import java.util.ArrayList;

public class ExploreEventsFragment extends Fragment {

    ListView cardListView;
    ExploreEventsCardAdapter cardAdapter;

    public ExploreEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explore_events, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();

        ArrayList<String> cardDataList = new ArrayList<>();
        cardDataList.add("event_1");
        cardDataList.add("event_2");
        cardDataList.add("event_3");

        cardListView = rootView.findViewById(R.id.explore_events_listView);
        cardAdapter = new ExploreEventsCardAdapter(rootView.getContext(), cardDataList);
        cardListView.setAdapter(cardAdapter);

        cardListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedEventId = cardDataList.get(position);

            Bundle bundle = new Bundle();
            bundle.putString("eventId", selectedEventId);

            assert mainActivity != null;
            mainActivity.navController.navigate(
                    R.id.action_userExploreEventsFragment_to_eventDetailsFragment,
                    bundle
            );
        });

        return rootView;
    }
}