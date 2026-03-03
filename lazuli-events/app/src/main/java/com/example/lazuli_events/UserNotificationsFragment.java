package com.example.lazuli_events;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;


public class UserNotificationsFragment extends Fragment {

    ListView notificationListView;
    NotificationListAdapter notificationListAdapter;

    public UserNotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_user_notifications, container, false);
        NavGraphActivity navGraphActivity = (NavGraphActivity) getActivity();
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


        return rootView;
    }
}