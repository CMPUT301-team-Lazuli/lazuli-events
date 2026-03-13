package com.example.lazuli_events.events;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.lazuli_events.R;
import com.example.lazuli_events.data_organizer.EventRepository;
import com.example.lazuli_events.model.Event;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventDetailsFragment extends Fragment {

    // repository for loading event data and waitlist actions
    private final EventRepository repository = new EventRepository();

    // background thread for loading poster image from URL
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // id of the event passed from previous screen
    private String currentEventId;

    // tracks whether current user is already in the waitlist
    private boolean isUserInWaitlist = false;

    private ImageView ivEventPoster;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvWaitlistCount;
    private MaterialButton btnJoinWaitlist;

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate event details layout
        View rootView = inflater.inflate(R.layout.fragment_event_details, container, false);

        // get event id from fragment arguments
        Bundle args = getArguments();
        if (args != null) {
            currentEventId = args.getString("eventId");
        }

        // connect XML views to Java
        ivEventPoster = rootView.findViewById(R.id.ivEventPoster);
        tvTitle = rootView.findViewById(R.id.event_details_title);
        tvDescription = rootView.findViewById(R.id.event_details_view_description);
        tvWaitlistCount = rootView.findViewById(R.id.event_details_waitlist_count_text);
        btnJoinWaitlist = rootView.findViewById(R.id.btnJoinWaitlist);

        if (btnJoinWaitlist != null) {
            btnJoinWaitlist.setOnClickListener(v -> {
                // user must be logged in to join or leave waitlist
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(requireContext(), "You must be logged in first", Toast.LENGTH_LONG).show();
                    return;
                }

                // event id must exist before doing any waitlist action
                if (TextUtils.isEmpty(currentEventId)) {
                    Toast.makeText(requireContext(), "Event ID not found", Toast.LENGTH_LONG).show();
                    return;
                }

                // current logged-in user id
                String entrantId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                if (!isUserInWaitlist) {
                    // add user to waitlist
                    repository.joinWaitlist(currentEventId, entrantId, new EventRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Added to waitlist", Toast.LENGTH_LONG).show();
                            isUserInWaitlist = true;
                            btnJoinWaitlist.setText("Leave Waitlist");
                            loadEvent(); // refresh UI after joining
                        }

                        @Override
                        public void onError(Exception e) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    // remove user from waitlist
                    repository.leaveWaitlist(currentEventId, entrantId, new EventRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Removed from waitlist", Toast.LENGTH_LONG).show();
                            isUserInWaitlist = false;
                            btnJoinWaitlist.setText("Join Waitlist");
                            loadEvent(); // refresh UI after leaving
                        }

                        @Override
                        public void onError(Exception e) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }

        // load event details when fragment opens
        loadEvent();
        return rootView;
    }

    private void loadEvent() {
        // stop if event id is missing
        if (TextUtils.isEmpty(currentEventId)) {
            return;
        }

        // fetch event document from Firestore
        repository.getEventById(currentEventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                if (!isAdded()) return;

                // show event title
                if (tvTitle != null && !TextUtils.isEmpty(event.getName())) {
                    tvTitle.setText(event.getName());
                }

                // show event description
                if (tvDescription != null && !TextUtils.isEmpty(event.getDescription())) {
                    tvDescription.setText(event.getDescription());
                }

                // load poster image if event has poster URL
                if (ivEventPoster != null && !TextUtils.isEmpty(event.getPosterUrl())) {
                    loadPosterFromUrl(event.getPosterUrl());
                }

                // update waitlist count and button state
                updateWaitlistState(event);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateWaitlistState(Event event) {
        // show current waitlist count
        if (tvWaitlistCount != null) {
            tvWaitlistCount.setText(event.getWaitlistCount() + " people on waitlist");
        }

        // if no user is logged in, always show join button
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            isUserInWaitlist = false;

            if (btnJoinWaitlist != null) {
                btnJoinWaitlist.setText("Join Waitlist");
            }
            return;
        }

        String entrantId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // check whether current user is already in this event's waitlist
        repository.isUserInWaitlist(currentEventId, entrantId, new EventRepository.WaitlistStatusCallback() {
            @Override
            public void onResult(boolean inWaitlist) {
                if (!isAdded()) return;

                isUserInWaitlist = inWaitlist;

                // update button text based on waitlist status
                if (btnJoinWaitlist != null) {
                    btnJoinWaitlist.setText(isUserInWaitlist ? "Leave Waitlist" : "Join Waitlist");
                }
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadPosterFromUrl(String urlString) {
        // load poster image in background thread
        executor.execute(() -> {
            try {
                InputStream inputStream = new URL(urlString).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // update image on main thread
                if (isAdded() && bitmap != null) {
                    requireActivity().runOnUiThread(() -> ivEventPoster.setImageBitmap(bitmap));
                }
            } catch (Exception ignored) {
                // ignore poster loading errors
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // stop background thread when fragment is destroyed
        executor.shutdown();
    }
}