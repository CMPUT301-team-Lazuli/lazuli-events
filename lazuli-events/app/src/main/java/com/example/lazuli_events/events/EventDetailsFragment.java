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

    private final EventRepository repository = new EventRepository();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String currentEventId;
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
        View rootView = inflater.inflate(R.layout.fragment_event_details, container, false);

        Bundle args = getArguments();
        if (args != null) {
            currentEventId = args.getString("eventId");
        }

        ivEventPoster = rootView.findViewById(R.id.ivEventPoster);
        tvTitle = rootView.findViewById(R.id.event_details_title);
        tvDescription = rootView.findViewById(R.id.event_details_view_description);
        tvWaitlistCount = rootView.findViewById(R.id.event_details_waitlist_count_text);
        btnJoinWaitlist = rootView.findViewById(R.id.btnJoinWaitlist);

        if (btnJoinWaitlist != null) {
            btnJoinWaitlist.setOnClickListener(v -> {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(requireContext(), "You must be logged in first", Toast.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(currentEventId)) {
                    Toast.makeText(requireContext(), "Event ID not found", Toast.LENGTH_LONG).show();
                    return;
                }

                String entrantId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                if (!isUserInWaitlist) {
                    repository.joinWaitlist(currentEventId, entrantId, new EventRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Added to waitlist", Toast.LENGTH_LONG).show();
                            isUserInWaitlist = true;
                            btnJoinWaitlist.setText("Leave Waitlist");
                            loadEvent();
                        }

                        @Override
                        public void onError(Exception e) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    repository.leaveWaitlist(currentEventId, entrantId, new EventRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Removed from waitlist", Toast.LENGTH_LONG).show();
                            isUserInWaitlist = false;
                            btnJoinWaitlist.setText("Join Waitlist");
                            loadEvent();
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

        loadEvent();
        return rootView;
    }

    private void loadEvent() {
        if (TextUtils.isEmpty(currentEventId)) {
            return;
        }

        repository.getEventById(currentEventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                if (!isAdded()) return;

                if (tvTitle != null && !TextUtils.isEmpty(event.getName())) {
                    tvTitle.setText(event.getName());
                }

                if (tvDescription != null && !TextUtils.isEmpty(event.getDescription())) {
                    tvDescription.setText(event.getDescription());
                }

                if (ivEventPoster != null && !TextUtils.isEmpty(event.getPosterUrl())) {
                    loadPosterFromUrl(event.getPosterUrl());
                }

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
        if (tvWaitlistCount != null) {
            tvWaitlistCount.setText(event.getWaitlistCount() + " people on waitlist");
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            isUserInWaitlist = false;

            if (btnJoinWaitlist != null) {
                btnJoinWaitlist.setText("Join Waitlist");
            }
            return;
        }

        String entrantId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        repository.isUserInWaitlist(currentEventId, entrantId, new EventRepository.WaitlistStatusCallback() {
            @Override
            public void onResult(boolean inWaitlist) {
                if (!isAdded()) return;

                isUserInWaitlist = inWaitlist;

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
        executor.execute(() -> {
            try {
                InputStream inputStream = new URL(urlString).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                if (isAdded() && bitmap != null) {
                    requireActivity().runOnUiThread(() -> ivEventPoster.setImageBitmap(bitmap));
                }
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}