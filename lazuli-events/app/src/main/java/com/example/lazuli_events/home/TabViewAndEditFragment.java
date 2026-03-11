package com.example.lazuli_events.home;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.lazuli_events.R;
import com.example.lazuli_events.data.EventRepository;
import com.example.lazuli_events.model.Event;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TabViewAndEditFragment extends Fragment {

    private final EventRepository repository = new EventRepository();

    private Uri selectedPosterUri;
    private ImageView ivPosterPreview;

    private TextInputEditText etEventName;
    private TextInputEditText etDescription;
    private TextInputEditText etLocation;
    private TextInputEditText etContact;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPosterUri = uri;
                    ivPosterPreview.setImageURI(uri);
                }
            });

    public TabViewAndEditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_view_and_edit, container, false);

        ivPosterPreview = rootView.findViewById(R.id.ivPosterPreview);
        MaterialButton btnAddCover = rootView.findViewById(R.id.btnAddCover);
        MaterialButton btnSaveEvent = rootView.findViewById(R.id.btnSaveEvent);

        etEventName = rootView.findViewById(R.id.etEventName);
        etDescription = rootView.findViewById(R.id.etDescription);
        etLocation = rootView.findViewById(R.id.etLocation);
        etContact = rootView.findViewById(R.id.etContact);

        btnAddCover.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSaveEvent.setOnClickListener(v -> saveEvent());

        return rootView;
    }

    private void saveEvent() {
        String name = getText(etEventName);
        String description = getText(etDescription);
        String location = getText(etLocation);
        String contact = getText(etContact);

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), "Event name is required", Toast.LENGTH_LONG).show();
            return;
        }

        String organizerId = FirebaseAuth.getInstance().getCurrentUser() == null
                ? ""
                : FirebaseAuth.getInstance().getCurrentUser().getUid();

        String eventId = FirebaseFirestore.getInstance()
                .collection("events")
                .document()
                .getId();

        Event event = new Event();
        event.setId(eventId);
        event.setOrganizerId(organizerId);
        event.setName(name);
        event.setDescription(description);
        event.setLocation(location);
        event.setContact(contact);

        if (selectedPosterUri != null) {
            repository.uploadPoster(eventId, selectedPosterUri, new EventRepository.PosterUploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    event.setPosterUrl(downloadUrl);
                    saveEventDocument(event);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            saveEventDocument(event);
        }
    }

    private void saveEventDocument(Event event) {
        repository.saveEvent(event, new EventRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Event saved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}