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
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class TabViewAndEditFragment extends Fragment {

    private final EventRepository repository = new EventRepository();

    private Uri selectedPosterUri;

    private ImageView ivPosterPreview;
    private TextView tvPosterPlaceholder;

    private TextInputEditText etEventName;
    private TextInputEditText etRegistrationStart;
    private TextInputEditText etRegistrationEnd;
    private TextInputEditText etLocation;
    private TextInputEditText etWaitlistCap;
    private TextInputEditText etDescription;
    private TextInputEditText etContact;

    private MaterialAutoCompleteTextView actEventType;
    private MaterialAutoCompleteTextView actWhoCanAttend;

    private Long registrationStartMillis;
    private Long registrationEndMillis;

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

    public TabViewAndEditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_event_manager, container, false);

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
        etWaitlistCap = rootView.findViewById(R.id.etWaitlistCap);
        etDescription = rootView.findViewById(R.id.etDescription);
        etContact = rootView.findViewById(R.id.etContact);

        actEventType = rootView.findViewById(R.id.actEventType);
        actWhoCanAttend = rootView.findViewById(R.id.actWhoCanAttend);

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

    private void saveEvent(boolean showQrAfterSave) {
        String name = getText(etEventName);
        String location = getText(etLocation);
        String description = getText(etDescription);
        String contact = getText(etContact);
        String waitlistCapRaw = getText(etWaitlistCap);

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), "Event name is required", Toast.LENGTH_LONG).show();
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
                            "Spots open must be greater than 0 or left blank",
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
        event.setWaitlistCap(waitlistCap);
        event.setWaitlistCount(0);
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

    private String buildQrPayload(String eventId) {
        return "lazuli://event/" + eventId;
    }

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

    private String formatDateTime(long millis) {
        return new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(millis);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
