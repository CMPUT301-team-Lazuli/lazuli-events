package com.example.lazuli_events.events;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lazuli_events.FirebaseDB;
import com.example.lazuli_events.MainActivity;
import com.example.lazuli_events.R;
import com.example.lazuli_events.profile.Profile;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class EventDetailsFragment extends Fragment {

    private String eventId;
    private boolean isUserInWaitlist = false;
    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView waitlistCountTextView;
    private MaterialButton waitlistButton;

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_details, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) {
            return rootView;
        }

        titleTextView = rootView.findViewById(R.id.event_details_title);
        descriptionTextView = rootView.findViewById(R.id.event_details_view_description);
        waitlistCountTextView = rootView.findViewById(R.id.event_details_waitlist_count_text);
        waitlistButton = rootView.findViewById(R.id.event_waitlist_button);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
        }

        if (eventId == null) {
            Toast.makeText(getContext(), "Missing event ID", Toast.LENGTH_SHORT).show();
            return rootView;
        }

        loadEvent(mainActivity);

        waitlistButton.setOnClickListener(v -> {
            Profile sessionProfile = mainActivity.sessionProfile;

            if (sessionProfile == null) {
                Toast.makeText(getContext(), "No signed-in user profile found", Toast.LENGTH_SHORT).show();
                return;
            }

            String entrantId = sessionProfile.getEmail(); // simple unique id for now

            if (!isUserInWaitlist) {
                mainActivity.firebaseDB.joinWaitlist(eventId, entrantId, new FirebaseDB.SimpleCallback() {
                    @Override
                    public void onSuccess(String message) {
                        if (getActivity() == null) return;
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        isUserInWaitlist = true;
                        waitlistButton.setText("Leave Waitlist");
                        loadEvent(mainActivity);
                    }

                    @Override
                    public void onFailure(String error) {
                        if (getActivity() == null) return;
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                mainActivity.firebaseDB.leaveWaitlist(eventId, entrantId, new FirebaseDB.SimpleCallback() {
                    @Override
                    public void onSuccess(String message) {
                        if (getActivity() == null) return;
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        isUserInWaitlist = false;
                        waitlistButton.setText("Join Waitlist");
                        loadEvent(mainActivity);
                    }

                    @Override
                    public void onFailure(String error) {
                        if (getActivity() == null) return;
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return rootView;
    }

    private void loadEvent(MainActivity mainActivity) {
        mainActivity.firebaseDB.getEventById(eventId,
                documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String title = documentSnapshot.getString("title");
                    String description = documentSnapshot.getString("description");
                    ArrayList<String> waitlist = (ArrayList<String>) documentSnapshot.get("waitlist");

                    if (title != null) {
                        titleTextView.setText(title);
                    }

                    if (description != null) {
                        descriptionTextView.setText(description);
                    }

                    int waitlistSize = (waitlist == null) ? 0 : waitlist.size();
                    waitlistCountTextView.setText(waitlistSize + " people on waitlist");

                    Profile sessionProfile = mainActivity.sessionProfile;
                    String entrantId = (sessionProfile == null) ? null : sessionProfile.getEmail();

                    isUserInWaitlist = waitlist != null && entrantId != null && waitlist.contains(entrantId);
                    waitlistButton.setText(isUserInWaitlist ? "Leave Waitlist" : "Join Waitlist");
                },
                e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}