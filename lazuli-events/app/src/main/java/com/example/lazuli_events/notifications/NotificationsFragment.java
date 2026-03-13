package com.example.lazuli_events.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.lazuli_events.MainActivity;
import com.example.lazuli_events.R;
import com.example.lazuli_events.profile.Profile;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    ListView notificationListView;
    NotificationListAdapter notificationListAdapter;
    ArrayList<UserNotification> notificationDataList;

    public NotificationsFragment() {
        // required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate notifications screen layout
        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();

        // connect list view with adapter
        notificationListView = rootView.findViewById(R.id.notifications_listView);
        notificationDataList = new ArrayList<>();
        notificationListAdapter = new NotificationListAdapter(rootView.getContext(), notificationDataList);
        notificationListView.setAdapter(notificationListAdapter);

        // load notifications for current signed-in user
        if (mainActivity != null && mainActivity.sessionProfile != null) {
            Profile profile = mainActivity.sessionProfile;
            String recipientId = profile.getEmail();

            mainActivity.firebaseDB.getNotificationsForUser(recipientId, new com.example.lazuli_events.FirebaseDB.NotificationsCallback() {
                @Override
                public void onSuccess(ArrayList<UserNotification> notifications) {
                    // replace old list with newest data from Firestore
                    notificationDataList.clear();
                    notificationDataList.addAll(notifications);
                    notificationListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(String error) {
                    // show database error message
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // no logged-in profile found
            Toast.makeText(getContext(), "No session profile found", Toast.LENGTH_SHORT).show();
        }

        // open notification settings page
        ImageButton settingsButton = rootView.findViewById(R.id.notification_settings_button);
        settingsButton.setOnClickListener(v -> {
            if (mainActivity != null) {
                mainActivity.navController.navigate(R.id.action_userNotificationsFragment_to_notificationSettingsFragment);
            }
        });

        return rootView;
    }
}
