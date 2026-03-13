package com.example.lazuli_events.home;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lazuli_events.data_organizer.EventRepository;
import com.example.lazuli_events.databinding.ActivityCreateEditEventBinding;
import com.example.lazuli_events.model.Event;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateEditEventActivity extends AppCompatActivity {

    private ActivityCreateEditEventBinding binding;
    private final EventRepository repository = new EventRepository();

    private Uri selectedPosterUri;
    private Long registrationStartMillis;
    private Long registrationEndMillis;

    // For edit mode
    private String editingEventId;
    private int existingWaitlistCount = 0;
    private Long existingCreatedAt;
    private String existingPosterUrl;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPosterUri = uri;
                    binding.ivPosterPreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateEditEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editingEventId = getIntent().getStringExtra("EVENT_ID");

        binding.btnAddCover.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        binding.etRegistrationStart.setOnClickListener(v -> showDateTimePicker(true));
        binding.etRegistrationEnd.setOnClickListener(v -> showDateTimePicker(false));

        binding.btnSaveEvent.setOnClickListener(v -> saveEvent());

        // If you already have edit flow, load data here.
        // Example:
        // if (editingEventId != null) { loadEvent(editingEventId); }
    }

    private void showDateTimePicker(boolean isStartField) {
        Calendar now = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(Calendar.YEAR, year);
                    picked.set(Calendar.MONTH, month);
                    picked.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                picked.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                picked.set(Calendar.MINUTE, minute);
                                picked.set(Calendar.SECOND, 0);
                                picked.set(Calendar.MILLISECOND, 0);

                                long millis = picked.getTimeInMillis();
                                String formatted = formatDateTime(millis);

                                if (isStartField) {
                                    registrationStartMillis = millis;
                                    binding.etRegistrationStart.setText(formatted);
                                } else {
                                    registrationEndMillis = millis;
                                    binding.etRegistrationEnd.setText(formatted);
                                }
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            false
                    );
                    timePickerDialog.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void saveEvent() {
        String name = valueOf(binding.etEventName.getText());
        String location = valueOf(binding.etLocation.getText());
        String description = valueOf(binding.etDescription.getText());
        String contact = valueOf(binding.etContact.getText());
        String waitlistCapRaw = valueOf(binding.etWaitlistCap.getText());

        if (TextUtils.isEmpty(name)) {
            binding.etEventName.setError("Event name is required");
            return;
        }

        if (registrationStartMillis == null) {
            binding.etRegistrationStart.setError("Pick registration start");
            return;
        }

        if (registrationEndMillis == null) {
            binding.etRegistrationEnd.setError("Pick registration end");
            return;
        }

        if (registrationStartMillis >= registrationEndMillis) {
            Toast.makeText(this,
                    "Registration start must be before registration end",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Long waitlistCap = null;
        if (!TextUtils.isEmpty(waitlistCapRaw)) {
            try {
                long parsed = Long.parseLong(waitlistCapRaw);
                if (parsed <= 0) {
                    Toast.makeText(this,
                            "Waitlist cap must be greater than 0, or leave it blank",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                waitlistCap = parsed;
            } catch (NumberFormatException e) {
                Toast.makeText(this,
                        "Invalid waitlist cap",
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        String organizerId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            organizerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        Event event = new Event();
        event.setId(editingEventId);
        event.setOrganizerId(organizerId);
        event.setName(name);
        event.setLocation(location);
        event.setDescription(description);
        event.setContact(contact);
        event.setRegistrationStartMillis(registrationStartMillis);
        event.setRegistrationEndMillis(registrationEndMillis);
        event.setWaitlistCap(waitlistCap);
        event.setWaitlistCount(existingWaitlistCount);
        event.setCreatedAt(existingCreatedAt);
        event.setPosterUrl(existingPosterUrl);

        setSaving(true);

        repository.saveEvent(event, selectedPosterUri, new EventRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                setSaving(false);
                Toast.makeText(CreateEditEventActivity.this,
                        "Event saved successfully",
                        Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                setSaving(false);
                Toast.makeText(CreateEditEventActivity.this,
                        e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setSaving(boolean saving) {
        binding.btnSaveEvent.setEnabled(!saving);
        binding.btnSaveEvent.setText(saving ? "Saving..." : "Save Event");
    }

    private String formatDateTime(long millis) {
        return new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                .format(millis);
    }

    private String valueOf(CharSequence charSequence) {
        return charSequence == null ? "" : charSequence.toString().trim();
    }
}
