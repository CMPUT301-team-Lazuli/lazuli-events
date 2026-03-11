package com.example.lazuli_events.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.lazuli_events.FirebaseDB;
import com.example.lazuli_events.MainActivity;
import com.example.lazuli_events.R;
import com.example.lazuli_events.profile.Profile;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class EventDetailsFragment extends Fragment {

    // current event id passed from previous screen
    private String eventId;

    // tracks whether current user is already in waitlist
    private boolean isUserInWaitlist = false;

    private TextView heroTitleTextView;
    private TextView descriptionTextView;
    private TextView waitlistCountTextView;
    private MaterialButton waitlistButton;

    public EventDetailsFragment() {
        // required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate event details screen
        View rootView = inflater.inflate(R.layout.fragment_event_details, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) {
            return rootView;
        }

        // connect XML views to Java
        heroTitleTextView = rootView.findViewById(R.id.event_details_title);
        descriptionTextView = rootView.findViewById(R.id.event_details_view_description);
        waitlistCountTextView = rootView.findViewById(R.id.event_details_waitlist_count_text);
        waitlistButton = rootView.findViewById(R.id.event_waitlist_button);

        // get event id passed from explore screen
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
        }

        // stop if no event id was passed
        if (eventId == null) {
            Toast.makeText(getContext(), "Missing event ID", Toast.LENGTH_SHORT).show();
            return rootView;
        }

        // load event information from Firestore
        loadEvent(mainActivity);

        // join or leave waitlist when button is pressed
        waitlistButton.setOnClickListener(v -> {
            Profile sessionProfile = mainActivity.sessionProfile;

            // make sure a user profile exists
            if (sessionProfile == null) {
                Toast.makeText(getContext(), "No session profile found", Toast.LENGTH_SHORT).show();
                return;
            }

            // use email as simple unique entrant id
            String entrantId = sessionProfile.getEmail();

            if (!isUserInWaitlist) {
                // join waitlist
                mainActivity.firebaseDB.joinWaitlist(eventId, entrantId, new FirebaseDB.SimpleCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        isUserInWaitlist = true;
                        waitlistButton.setText("Leave Waitlist");
                        loadEvent(mainActivity);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // leave waitlist
                mainActivity.firebaseDB.leaveWaitlist(eventId, entrantId, new FirebaseDB.SimpleCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        isUserInWaitlist = false;
                        waitlistButton.setText("Join Waitlist");
                        loadEvent(mainActivity);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return rootView;
    }

    private void loadEvent(MainActivity mainActivity) {
        // read event data from Firestore
        mainActivity.firebaseDB.getEventById(
                eventId,
                documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // get event fields from Firestore document
                    String title = documentSnapshot.getString("title");
                    String description = documentSnapshot.getString("description");
                    ArrayList<String> waitlist = (ArrayList<String>) documentSnapshot.get("waitlist");

                    // update title text
                    if (title != null) {
                        heroTitleTextView.setText(title);
                    }

                    // update description text
                    if (description != null) {
                        descriptionTextView.setText(description);
                    }

                    // show current waitlist size
                    int waitlistSize = (waitlist == null) ? 0 : waitlist.size();
                    waitlistCountTextView.setText(waitlistSize + " people on waitlist");

                    // check whether current user is already in waitlist
                    Profile sessionProfile = mainActivity.sessionProfile;
                    String entrantId = (sessionProfile == null) ? null : sessionProfile.getEmail();

                    isUserInWaitlist = waitlist != null && entrantId != null && waitlist.contains(entrantId);

                    // update button text based on waitlist status
                    if (isUserInWaitlist) {
                        waitlistButton.setText("Leave Waitlist");
                    } else {
                        waitlistButton.setText("Join Waitlist");
                    }
                },
                e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}