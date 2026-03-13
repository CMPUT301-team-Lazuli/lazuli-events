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

/**
 * Activity for creating a new event or editing an existing one.
 *
 * <p>This screen allows the organizer to:</p>
 * <ul>
 *     <li>Enter event details such as name, location, description, and contact</li>
 *     <li>Select registration start and end date/time</li>
 *     <li>Set an optional waitlist capacity</li>
 *     <li>Select and preview an event poster image</li>
 *     <li>Save the event to Firebase through {@link EventRepository}</li>
 * </ul>
 *
 * <p>If an event ID is passed through the intent, the activity can be used in edit mode.
 * Otherwise, it behaves as a create-event screen.</p>
 */
public class CreateEditEventActivity extends AppCompatActivity {

    /** View binding for the create/edit event layout. */
    private ActivityCreateEditEventBinding binding;

    /** Repository used for saving and retrieving event data. */
    private final EventRepository repository = new EventRepository();

    /** URI of the poster image selected by the user. */
    private Uri selectedPosterUri;

    /** Registration start time in milliseconds since epoch. */
    private Long registrationStartMillis;

    /** Registration end time in milliseconds since epoch. */
    private Long registrationEndMillis;

    /** ID of the event being edited; null when creating a new event. */
    private String editingEventId;

    /** Existing waitlist count, preserved in edit mode. */
    private int existingWaitlistCount = 0;

    /** Existing creation timestamp, preserved in edit mode. */
    private Long existingCreatedAt;

    /** Existing poster URL, preserved in edit mode if no new poster is selected. */
    private String existingPosterUrl;

    /**
     * Activity result launcher used to let the user pick an image from device storage.
     *
     * <p>When an image is selected, its URI is stored and previewed in the poster ImageView.</p>
     */
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPosterUri = uri;
                    binding.ivPosterPreview.setImageURI(uri);
                }
            });

    /**
     * Initializes the activity, view binding, click listeners, and edit-mode state.
     *
     * @param savedInstanceState previously saved instance state, or null if none exists
     */
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

    /**
     * Displays a date picker followed by a time picker, then stores the selected
     * date/time in either the registration start field or end field.
     *
     * @param isStartField {@code true} if the selected date/time should be applied
     *                     to the registration start field, {@code false} for the end field
     */
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

    /**
     * Validates user input, constructs an {@link Event} object, and saves it using the repository.
     *
     * <p>This method checks:</p>
     * <ul>
     *     <li>Event name is not empty</li>
     *     <li>Registration start and end times are selected</li>
     *     <li>Registration start is before registration end</li>
     *     <li>Waitlist cap, if provided, is a valid positive number</li>
     * </ul>
     *
     * <p>If validation succeeds, the event is saved and the activity finishes on success.</p>
     */
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

    /**
     * Updates the UI to reflect whether the activity is currently saving an event.
     *
     * @param saving {@code true} if a save operation is in progress, {@code false} otherwise
     */
    private void setSaving(boolean saving) {
        binding.btnSaveEvent.setEnabled(!saving);
        binding.btnSaveEvent.setText(saving ? "Saving..." : "Save Event");
    }

    /**
     * Formats a millisecond timestamp into a user-friendly date/time string.
     *
     * @param millis the timestamp in milliseconds since epoch
     * @return a formatted date/time string
     */
    private String formatDateTime(long millis) {
        return new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                .format(millis);
    }

    /**
     * Converts a {@link CharSequence} to a trimmed {@link String}.
     *
     * <p>If the input is null, an empty string is returned.</p>
     *
     * @param charSequence the character sequence to convert
     * @return the trimmed string value, or an empty string if null
     */
    private String valueOf(CharSequence charSequence) {
        return charSequence == null ? "" : charSequence.toString().trim();
    }
}