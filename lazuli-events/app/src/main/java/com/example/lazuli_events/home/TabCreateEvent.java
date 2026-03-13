package com.example.lazuli_events.home;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.lazuli_events.R;
import com.example.lazuli_events.data_organizer.EventRepository;
import com.example.lazuli_events.model.Event;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Fragment that allows an organizer to create a new event.
 *
 * <p>This fragment lets the user:</p>
 * <ul>
 *     <li>Enter event details such as name, location, description, and contact</li>
 *     <li>Select event type, audience, and waitlist capacity</li>
 *     <li>Choose registration opening and closing times</li>
 *     <li>Select a poster image for the event</li>
 *     <li>Save the event to Firestore using {@link EventRepository}</li>
 *     <li>Optionally generate and display a promotional QR code after saving</li>
 * </ul>
 */
public class TabCreateEvent extends Fragment {

    /** Repository used to save event data and upload poster images. */
    private final EventRepository repository = new EventRepository();

    /** URI of the poster image selected by the user. */
    private Uri selectedPosterUri;

    /** ImageView used to preview the selected poster image. */
    private ImageView ivPosterPreview;

    /** Placeholder text shown when no poster image has been selected. */
    private TextView tvPosterPlaceholder;

    /** Input field for the event name. */
    private TextInputEditText etEventName;

    /** Input field showing the selected registration start date and time. */
    private TextInputEditText etRegistrationStart;

    /** Input field showing the selected registration end date and time. */
    private TextInputEditText etRegistrationEnd;

    /** Input field for the event location. */
    private TextInputEditText etLocation;

    /** Input field for the event description. */
    private TextInputEditText etDescription;

    /** Input field for contact information. */
    private TextInputEditText etContact;

    /** Dropdown for selecting the event type. */
    private MaterialAutoCompleteTextView actEventType;

    /** Dropdown for selecting who can attend the event. */
    private MaterialAutoCompleteTextView actWhoCanAttend;

    /** Dropdown for selecting waitlist capacity. */
    private MaterialAutoCompleteTextView actWaitlistCap;

    /** Registration start time in milliseconds since epoch. */
    private Long registrationStartMillis;

    /** Registration end time in milliseconds since epoch. */
    private Long registrationEndMillis;

    /** Available options for event type selection. */
    private final String[] eventTypeOptions = {
            "Arts & Entertainment",
            "Sports & Fitness",
            "Food & Drink",
            "Technology & Gaming",
            "Workshop & Education"
    };

    /** Available options for who can attend the event. */
    private final String[] audienceOptions = {
            "All ages",
            "18+",
            "16+",
            "12+",
            "Kids"
    };

    /** Available preset options for waitlist capacity. */
    private final String[] waitlistCapOptions = {
            "10",
            "25",
            "50",
            "100",
            "250"
    };

    /**
     * Activity result launcher used to open the system picker for selecting a poster image.
     *
     * <p>When an image is selected, the poster preview is updated and the placeholder text
     * is hidden.</p>
     */
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPosterUri = uri;
                    ivPosterPreview.setImageURI(uri);

                    if (tvPosterPlaceholder != null) {
                        tvPosterPlaceholder.setVisibility(View.GONE);
                    }

                    Toast.makeText(requireContext(), "Cover selected", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Required empty public constructor.
     */
    public TabCreateEvent() {
    }

    /**
     * Inflates the fragment layout, binds views, initializes dropdowns,
     * and sets up click listeners for poster selection, date/time picking,
     * and saving the event.
     *
     * @param inflater the LayoutInflater used to inflate views
     * @param container the parent view that the fragment UI should attach to
     * @param savedInstanceState previously saved state, or {@code null} if none exists
     * @return the root view of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_event, container, false);

        ivPosterPreview = rootView.findViewById(R.id.ivPosterPreview);
        tvPosterPlaceholder = rootView.findViewById(R.id.tvPosterPlaceholder);

        MaterialButton btnAddCover = rootView.findViewById(R.id.btnAddCover);
        MaterialButton btnSaveEvent = rootView.findViewById(R.id.btnSaveEvent);

        MaterialCardView cardGenerateQr = rootView.findViewById(R.id.cardGenerateQr);
        View tvHeaderQrAction = rootView.findViewById(R.id.tvHeaderQrAction);

        etEventName = rootView.findViewById(R.id.etEventName);
        etRegistrationStart = rootView.findViewById(R.id.etRegistrationStart);
        etRegistrationEnd = rootView.findViewById(R.id.etRegistrationEnd);
        etLocation = rootView.findViewById(R.id.etLocation);
        etDescription = rootView.findViewById(R.id.etDescription);
        etContact = rootView.findViewById(R.id.etContact);

        actEventType = rootView.findViewById(R.id.actEventType);
        actWhoCanAttend = rootView.findViewById(R.id.actWhoCanAttend);
        actWaitlistCap = rootView.findViewById(R.id.actWaitlistCap);

        setupDropdowns();

        btnAddCover.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        etRegistrationStart.setOnClickListener(v -> openDateThenTimePicker(true));
        etRegistrationEnd.setOnClickListener(v -> openDateThenTimePicker(false));

        btnSaveEvent.setOnClickListener(v -> saveEvent(false));

        if (cardGenerateQr != null) {
            cardGenerateQr.setOnClickListener(v -> saveEvent(true));
        }

        if (tvHeaderQrAction != null) {
            tvHeaderQrAction.setOnClickListener(v -> saveEvent(true));
        }

        return rootView;
    }

    /**
     * Sets up dropdown adapters and click behavior for event type,
     * audience, and waitlist capacity fields.
     */
    private void setupDropdowns() {
        ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                eventTypeOptions
        );

        ArrayAdapter<String> audienceAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                audienceOptions
        );

        ArrayAdapter<String> waitlistCapAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                waitlistCapOptions
        );

        actEventType.setAdapter(eventTypeAdapter);
        actWhoCanAttend.setAdapter(audienceAdapter);
        actWaitlistCap.setAdapter(waitlistCapAdapter);

        actEventType.setOnClickListener(v -> actEventType.showDropDown());
        actWhoCanAttend.setOnClickListener(v -> actWhoCanAttend.showDropDown());
        actWaitlistCap.setOnClickListener(v -> actWaitlistCap.showDropDown());
    }

    /**
     * Opens a Material date picker and, after a date is selected,
     * opens a time picker for the same field.
     *
     * @param isStart {@code true} to pick the registration start date/time,
     *                {@code false} for the registration end date/time
     */
    private void openDateThenTimePicker(boolean isStart) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isStart ? "Registration opens" : "Registration closes")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selectionUtcMillis ->
                openTimePicker(selectionUtcMillis, isStart));

        datePicker.show(getParentFragmentManager(),
                isStart ? "registration_start_date" : "registration_end_date");
    }

    /**
     * Opens a Material time picker for the given selected date and stores
     * the final local timestamp in either the registration start or end field.
     *
     * @param selectedDateUtcMillis the selected date from the date picker in UTC milliseconds
     * @param isStart {@code true} to store the value as registration start,
     *                {@code false} to store it as registration end
     */
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
            String formatted = formatDateTime(millis);

            if (isStart) {
                registrationStartMillis = millis;
                etRegistrationStart.setText(formatted);
            } else {
                registrationEndMillis = millis;
                etRegistrationEnd.setText(formatted);
            }
        });

        timePicker.show(getParentFragmentManager(),
                isStart ? "registration_start_time" : "registration_end_time");
    }

    /**
     * Validates form input, builds an {@link Event} object, and saves it through the repository.
     *
     * <p>If saving succeeds and {@code showQrAfterSave} is true, a QR code dialog is displayed.
     * Otherwise, only a success toast is shown.</p>
     *
     * @param showQrAfterSave {@code true} to display a generated QR code after saving,
     *                        {@code false} to only save the event
     */
    private void saveEvent(boolean showQrAfterSave) {
        String name = getText(etEventName);
        String location = getText(etLocation);
        String description = getText(etDescription);
        String contact = getText(etContact);

        String eventType = getDropdownText(actEventType);
        String whoCanAttend = getDropdownText(actWhoCanAttend);
        String waitlistCapRaw = getDropdownText(actWaitlistCap);

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), "Event name is required", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(eventType)) {
            Toast.makeText(requireContext(), "Please choose an event type", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(whoCanAttend)) {
            Toast.makeText(requireContext(), "Please choose who can attend", Toast.LENGTH_LONG).show();
            return;
        }

        if (registrationStartMillis == null || registrationEndMillis == null) {
            Toast.makeText(requireContext(),
                    "Please choose registration open and close times",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (registrationStartMillis >= registrationEndMillis) {
            Toast.makeText(requireContext(),
                    "Registration open time must be before close time",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Long waitlistCap = null;
        if (!TextUtils.isEmpty(waitlistCapRaw)) {
            try {
                long parsed = Long.parseLong(waitlistCapRaw);
                if (parsed <= 0) {
                    Toast.makeText(requireContext(),
                            "Spots open must be greater than 0",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                waitlistCap = parsed;
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(),
                        "Invalid spots open number",
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        String organizerId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            organizerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        Event event = new Event();
        event.setOrganizerId(organizerId);
        event.setName(name);
        event.setLocation(location);
        event.setDescription(description);
        event.setContact(contact);
        event.setEventType(eventType);
        event.setWhoCanAttend(whoCanAttend);
        event.setWaitlistCap(waitlistCap);
        event.setWaitlistCount(0);
        event.setWaitlist(new ArrayList<>());
        event.setRegistrationStartMillis(registrationStartMillis);
        event.setRegistrationEndMillis(registrationEndMillis);

        repository.saveEvent(event, selectedPosterUri, new EventRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                if (showQrAfterSave) {
                    Toast.makeText(requireContext(),
                            "Event saved and QR generated",
                            Toast.LENGTH_LONG).show();
                    showGeneratedQrDialog(event.getId());
                } else {
                    Toast.makeText(requireContext(),
                            "Event saved",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Displays a dialog containing a generated QR code for the saved event.
     *
     * <p>The QR code encodes a payload built from the event ID.</p>
     *
     * @param eventId the ID of the saved event
     */
    private void showGeneratedQrDialog(String eventId) {
        try {
            String payload = buildQrPayload(eventId);
            Bitmap qrBitmap = createQrBitmap(payload, 900);

            View dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_generated_qr, null, false);

            ImageView ivGeneratedQr = dialogView.findViewById(R.id.ivGeneratedQr);
            TextView tvGeneratedQrPayload = dialogView.findViewById(R.id.tvGeneratedQrPayload);

            ivGeneratedQr.setImageBitmap(qrBitmap);
            tvGeneratedQrPayload.setText(payload);

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Promotional QR Code")
                    .setView(dialogView)
                    .setPositiveButton("Done", null)
                    .show();

        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    "Failed to generate QR code",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Builds the QR payload string for an event.
     *
     * @param eventId the event ID
     * @return a QR payload in the format {@code lazuli://event/{eventId}}
     */
    private String buildQrPayload(String eventId) {
        return "lazuli://event/" + eventId;
    }

    /**
     * Generates a QR code bitmap from the given content string.
     *
     * @param content the content to encode in the QR code
     * @param sizePx the width and height of the resulting bitmap in pixels
     * @return the generated QR code bitmap
     * @throws Exception if QR code generation fails
     */
    private Bitmap createQrBitmap(String content, int sizePx) throws Exception {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                sizePx,
                sizePx
        );

        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < sizePx; x++) {
            for (int y = 0; y < sizePx; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bitmap;
    }

    /**
     * Formats a millisecond timestamp as a readable date and time string.
     *
     * @param millis the timestamp in milliseconds since epoch
     * @return the formatted date/time string
     */
    private String formatDateTime(long millis) {
        return new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(millis);
    }

    /**
     * Gets trimmed text from a {@link TextInputEditText}.
     *
     * @param editText the input field
     * @return the trimmed text value, or an empty string if null
     */
    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    /**
     * Gets trimmed text from a {@link MaterialAutoCompleteTextView}.
     *
     * @param dropdown the dropdown field
     * @return the trimmed selected or entered value, or an empty string if null
     */
    private String getDropdownText(MaterialAutoCompleteTextView dropdown) {
        return dropdown.getText() == null ? "" : dropdown.getText().toString().trim();
    }
}