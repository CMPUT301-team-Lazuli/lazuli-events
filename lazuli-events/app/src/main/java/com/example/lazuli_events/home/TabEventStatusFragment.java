package com.example.lazuli_events.home;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.lazuli_events.FirebaseDB;
import com.example.lazuli_events.R;
import com.example.lazuli_events.model.Event;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class TabEventStatusFragment extends Fragment {

    private TextInputEditText etRegistrationSummary;
    private TextView tvReadyToDraw;
    private TextView tvSpotsFilled;
    private MaterialButton btnSaveRegistrationPeriod;
    private MaterialButton btnDrawLottery;

    private Long registrationStartMillis;
    private Long registrationEndMillis;

    private final FirebaseDB firebaseDB = new FirebaseDB();

    // Temporary event object for now.
    // Replace this later with the actual selected event loaded from Firestore.
    private Event currentEvent;

    public TabEventStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_event_status, container, false);

        etRegistrationSummary = rootView.findViewById(R.id.etRegistrationSummary);
        tvReadyToDraw = rootView.findViewById(R.id.tvReadyToDraw);
        tvSpotsFilled = rootView.findViewById(R.id.tvSpotsFilled);
        btnSaveRegistrationPeriod = rootView.findViewById(R.id.btnSaveRegistrationPeriod);
        btnDrawLottery = rootView.findViewById(R.id.btnDrawLottery);

        // TEMP DEMO EVENT
        currentEvent = new Event();
        currentEvent.setId("PUT_REAL_EVENT_ID_HERE");
        currentEvent.setWaitlistCount(12);
        currentEvent.setWaitlistCap(20L);

        registrationStartMillis = currentEvent.getRegistrationStartMillis();
        registrationEndMillis = currentEvent.getRegistrationEndMillis();

        etRegistrationSummary.setOnClickListener(v -> openDateThenTimePicker(true));
        btnSaveRegistrationPeriod.setOnClickListener(v -> openDateThenTimePicker(true));

        btnDrawLottery.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Draw Lottery clicked", Toast.LENGTH_SHORT).show()
        );

        bindEventStatusToUi();

        return rootView;
    }

    private void bindEventStatusToUi() {
        etRegistrationSummary.setText(buildRegistrationSummary());

        long waitlistCount = currentEvent.getWaitlistCount();
        Long waitlistCap = currentEvent.getWaitlistCap();

        if (waitlistCap != null) {
            tvSpotsFilled.setText(waitlistCount + "/" + waitlistCap + " spots filled");

            if (waitlistCount >= waitlistCap) {
                tvReadyToDraw.setText("Ready to Draw!");
            } else {
                tvReadyToDraw.setText("Not ready to draw yet");
            }
        } else {
            tvSpotsFilled.setText(waitlistCount + "/∞ spots filled");
            tvReadyToDraw.setText("Unlimited spots");
        }
    }

    private void openDateThenTimePicker(boolean isStart) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isStart ? "Registration opens" : "Registration closes")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selectionUtcMillis ->
                openTimePicker(selectionUtcMillis, isStart));

        datePicker.show(getParentFragmentManager(),
                isStart ? "event_status_registration_start_date" : "event_status_registration_end_date");
    }

    private void openTimePicker(long selectedDateUtcMillis, boolean isStart) {
        int clockFormat = DateFormat.is24HourFormat(requireContext())
                ? TimeFormat.CLOCK_24H
                : TimeFormat.CLOCK_12H;

        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(9)
                .setMinute(0)
                .setTitleText(isStart ? "Select opening time" : "Select closing time")
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            utcCalendar.setTimeInMillis(selectedDateUtcMillis);

            int year = utcCalendar.get(Calendar.YEAR);
            int month = utcCalendar.get(Calendar.MONTH);
            int day = utcCalendar.get(Calendar.DAY_OF_MONTH);

            Calendar localCalendar = Calendar.getInstance();
            localCalendar.set(Calendar.YEAR, year);
            localCalendar.set(Calendar.MONTH, month);
            localCalendar.set(Calendar.DAY_OF_MONTH, day);
            localCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            localCalendar.set(Calendar.MINUTE, timePicker.getMinute());
            localCalendar.set(Calendar.SECOND, 0);
            localCalendar.set(Calendar.MILLISECOND, 0);

            long millis = localCalendar.getTimeInMillis();

            if (isStart) {
                registrationStartMillis = millis;
                openDateThenTimePicker(false);
            } else {
                registrationEndMillis = millis;
                saveRegistrationPeriod();
            }
        });

        timePicker.show(getParentFragmentManager(),
                isStart ? "event_status_registration_start_time" : "event_status_registration_end_time");
    }

    private void saveRegistrationPeriod() {
        if (registrationStartMillis == null || registrationEndMillis == null) {
            Toast.makeText(requireContext(),
                    "Please choose both registration open and close times",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (registrationStartMillis >= registrationEndMillis) {
            Toast.makeText(requireContext(),
                    "Registration open time must be before close time",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String registrationPeriodText =
                formatDateTime(registrationStartMillis) + " - " + formatDateTime(registrationEndMillis);

        currentEvent.setRegistrationStartMillis(registrationStartMillis);
        currentEvent.setRegistrationEndMillis(registrationEndMillis);
        currentEvent.setRegistrationPeriodText(registrationPeriodText);

        etRegistrationSummary.setText(buildRegistrationSummary());

        if (currentEvent.getId() == null || currentEvent.getId().trim().isEmpty()
                || currentEvent.getId().equals("PUT_REAL_EVENT_ID_HERE")) {
            Toast.makeText(requireContext(),
                    "Registration period updated locally. Set a real event ID to save to Firestore.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        firebaseDB.updateEventRegistrationPeriod(
                currentEvent.getId(),
                registrationStartMillis,
                registrationEndMillis,
                registrationPeriodText,
                new FirebaseDB.SimpleCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private String buildRegistrationSummary() {
        if (registrationStartMillis == null || registrationEndMillis == null) {
            return "date and time + open/closed + days left";
        }

        long now = System.currentTimeMillis();

        String status;
        if (now < registrationStartMillis) {
            status = "Not open yet";
        } else if (now <= registrationEndMillis) {
            status = "Open";
        } else {
            status = "Closed";
        }

        long millisLeft = registrationEndMillis - now;
        long daysLeft = Math.max(0, millisLeft / (1000L * 60 * 60 * 24));

        return formatDateTime(registrationStartMillis)
                + " - "
                + formatDateTime(registrationEndMillis)
                + " • "
                + status
                + " • "
                + daysLeft
                + " days left";
    }

    private String formatDateTime(long millis) {
        return new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(millis);
    }
}
