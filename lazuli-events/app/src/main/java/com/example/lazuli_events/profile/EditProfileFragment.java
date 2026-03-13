package com.example.lazuli_events.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lazuli_events.MainActivity;
import com.example.lazuli_events.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditProfileFragment extends Fragment {

    private static final String key = "sessionProfile";

    public EditProfileFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();

        // Fetch current user's session from MainActivity (specifically the nav)
        if (getArguments() == null){
            throw new NullPointerException("Issues reading profile. getArguments is null!");
        }

        Bundle profileBundle = getArguments();
        Profile profile = (Profile) profileBundle.getSerializable(key);
        if (profile == null){
            throw new NullPointerException("Issue: profile equals null.");
        }

        MaterialButton saveButton = rootView.findViewById(R.id.save_profile_changes_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Change name
                TextInputLayout nameInput = rootView.findViewById(R.id.edit_name_textfield);
                String newName = nameInput.getEditText().getText().toString();
                if (!newName.isEmpty()) {
                    profile.setName(newName);
                }

                //Change address
                //TextInputEditText addrInput = rootView.findViewById(R.id.edit_address_textfield);
                //TODO: add addresses to edit profile

                //Change phone
                TextInputLayout phoneInput = rootView.findViewById(R.id.edit_phone_textfield);
                String newPhone = phoneInput.getEditText().getText().toString();
                if (!newPhone.isEmpty()){
                    profile.setPhone(phoneInput.getEditText().getText().toString());
                }

                //Change email
                TextInputLayout emailInput = rootView.findViewById(R.id.edit_email_textfield);
                String newEmail = emailInput.getEditText().getText().toString();
                if (!newEmail.isEmpty()){
                    profile.setEmail(emailInput.getEditText().getText().toString());
                }

                //Pass changes to main (where they'll be asserted for validity)
                Bundle profileBundle = new Bundle();
                profileBundle.putSerializable(key, profile);
                assert mainActivity != null;
                mainActivity.navController.navigate(R.id.userProfileFragment, profileBundle);

            }
        });


        return rootView;
    }
}
