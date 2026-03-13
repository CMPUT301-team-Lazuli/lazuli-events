package com.example.lazuli_events.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.lazuli_events.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment responsible for allowing organizers to compose and send.
 * Notifications to specific groups of entrants (Registered, Waitlisted, Cancelled).
 */
public class ComposeNotificationFragment extends Fragment {

    private Spinner notificationTypeSpinner;
    private CheckBox registeredCheckbox;
    private CheckBox waitlistedCheckbox;
    private CheckBox cancelledCheckbox;
    private TextInputLayout messageInputLayout;
    private MaterialButton sendButton;

    /**
     * Required empty public constructor for fragment installment.
     */
    public ComposeNotificationFragment() {
    }

    /**
     * Called to have the fragment for its user interface view.
     * Inflates the layout and initializes the UI components.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_compose_notification, container, false);

        // initialize UI components using the IDs from XML
        notificationTypeSpinner = rootView.findViewById(R.id.notification_type_spinner);
        registeredCheckbox = rootView.findViewById(R.id.registered_checkbox);
        waitlistedCheckbox = rootView.findViewById(R.id.waitlisted_checkbox);
        cancelledCheckbox = rootView.findViewById(R.id.cancelled_checkbox);
        messageInputLayout = rootView.findViewById(R.id.edit_address_textfield);
        sendButton = rootView.findViewById(R.id.confirm_send_button);

        // set up the dropdown menu with options
        String[] notificationTypes = {"General Update", "Important Announcement", "Lottery Result", "Event Cancellation"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, notificationTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        notificationTypeSpinner.setAdapter(adapter);

        // set up the Send button click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the click event for the send button.
             * Validates the input and processes the notification dispatch.
             * @param v The view that was clicked (the send button).
             */
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        return rootView;
    }

    /**
     * Validates the form data and triggers the notification sending process.
     * Checks if the message is empty and if at least one recipient group is selected.
     */
    private void sendMessage() {
        String message = "";
        if (messageInputLayout.getEditText() != null) {
            message = messageInputLayout.getEditText().getText().toString().trim();
        }

        // validate that the user actually typed a message
        if (message.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a message.", Toast.LENGTH_SHORT).show();
            return;
        }

        // check which groups the organizer wants to send the message to
        boolean sendToRegistered = registeredCheckbox.isChecked();
        boolean sendToWaitlisted = waitlistedCheckbox.isChecked();
        boolean sendToCancelled = cancelledCheckbox.isChecked();

        // validate that at least one checkbox is checked
        if (!sendToRegistered && !sendToWaitlisted && !sendToCancelled) {
            Toast.makeText(getContext(), "Please select at least one recipient group.", Toast.LENGTH_SHORT).show();
            return;
        }

        String notifType = notificationTypeSpinner.getSelectedItem().toString();

        // this Toast verifies the UI logic successfully captured everything
        Toast.makeText(getContext(), "Ready to send " + notifType + " to Firebase!", Toast.LENGTH_SHORT).show();
    }
}