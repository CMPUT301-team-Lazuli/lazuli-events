package com.example.lazuli_events.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lazuli_events.FirebaseDB;
import com.example.lazuli_events.R;
import com.example.lazuli_events.model.Event;
import com.example.lazuli_events.profile.Profile;
import java.util.ArrayList;

/**
 * Fragment responsible for the entire Administrator Dashboard.
 * Allows switching between browsing profiles, events/images, and notification logs.
 * Fulfills US 03.01.01, 03.02.01, 03.03.01, 03.05.01, 03.06.01, 03.07.01, 03.08.01.
 */
public class AdminManageFragment extends Fragment {

    private Spinner spinnerViewType;
    private RecyclerView rvAdminContent;
    private FirebaseDB firebaseDB;

    public AdminManageFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerViewType = view.findViewById(R.id.spinnerAdminViewType);
        rvAdminContent = view.findViewById(R.id.rvAdminContent);
        rvAdminContent.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseDB = new FirebaseDB();

        String[] viewOptions = {"Browse Profiles & Organizers", "Browse Events & Images", "Review Notification Logs"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, viewOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerViewType.setAdapter(spinnerAdapter);

        spinnerViewType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rvAdminContent.setAdapter(null); // Clear previous view
                switch (position) {
                    case 0: loadProfiles(); break;
                    case 1: loadEvents(); break;
                    case 2: loadNotifications(); break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadProfiles() {
        ArrayList<Profile> profiles = firebaseDB.getProfiles();
        AdminProfileListAdapter adapter = new AdminProfileListAdapter(profiles, (profile, position) -> {
            try {
                firebaseDB.deleteProfileFromDB(profile);
                profiles.remove(position);
                rvAdminContent.getAdapter().notifyItemRemoved(position);
                Toast.makeText(getContext(), "Profile/Organizer Deleted", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        rvAdminContent.setAdapter(adapter);
        rvAdminContent.postDelayed(adapter::notifyDataSetChanged, 500); // Wait for snapshot listener
    }

    private void loadEvents() {
        firebaseDB.getAllEventsForAdmin(new FirebaseDB.EventsListCallback() {
            @Override
            public void onSuccess(ArrayList<Event> events) {
                AdminEventListAdapter adapter = new AdminEventListAdapter(events, new AdminEventListAdapter.OnAdminEventActionListener() {
                    @Override
                    public void onDeleteEventClick(Event event, int position) {
                        firebaseDB.deleteEvent(event, new FirebaseDB.SimpleCallback() {
                            @Override
                            public void onSuccess(String msg) {
                                events.remove(position);
                                rvAdminContent.getAdapter().notifyItemRemoved(position);
                                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onDeleteImageClick(Event event, int position) {
                        firebaseDB.deleteEventImageOnly(event, new FirebaseDB.SimpleCallback() {
                            @Override
                            public void onSuccess(String msg) {
                                event.setPosterUrl(null);
                                rvAdminContent.getAdapter().notifyItemChanged(position);
                                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                if (isAdded()) rvAdminContent.setAdapter(adapter);
            }
            @Override
            public void onFailure(String error) {
                if (isAdded()) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNotifications() {
        firebaseDB.getAllNotificationsForAdmin(new FirebaseDB.NotificationsCallback() {
            @Override
            public void onSuccess(ArrayList<com.example.lazuli_events.notifications.UserNotification> notifications) {
                AdminNotificationListAdapter adapter = new AdminNotificationListAdapter(notifications);
                if (isAdded()) {
                    rvAdminContent.setAdapter(adapter);
                    Toast.makeText(getContext(), "Loaded " + notifications.size() + " logs.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(String error) {
                if (isAdded()) Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}