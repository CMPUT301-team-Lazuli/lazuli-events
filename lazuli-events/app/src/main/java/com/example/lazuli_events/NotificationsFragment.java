package com.example.lazuli_events;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;


public class NotificationsFragment extends Fragment {

    ListView notificationListView;
    NotificationListAdapter notificationListAdapter;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_notifications, container, false);
        MainActivity navGraphActivity = (MainActivity) getActivity();
        // code here

        // placeholder notification list data
        ArrayList<String> notificationDataList = new ArrayList<>();
        notificationDataList.add("notification 1");
        notificationDataList.add("notification 2");
        notificationDataList.add("notification 4");
        notificationDataList.add("notification 5");
        notificationDataList.add("notification 6");
        notificationDataList.add("notification 7");
        notificationDataList.add("notification 8");
        notificationDataList.add("notification 9");
        notificationDataList.add("notification 10");
        notificationDataList.add("notification 11");

        // get list view and set adapter
        notificationListView = rootView.findViewById(R.id.notifications_listView);
        notificationListAdapter = new NotificationListAdapter(rootView.getContext(), notificationDataList);
        notificationListView.setAdapter(notificationListAdapter);

        // set notification settings button listener
//        Button settingsButton = rootView.findViewById(R.id.notification_settings_button);
//        settingsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pass
//            }
//        });

        return rootView;
    }
}