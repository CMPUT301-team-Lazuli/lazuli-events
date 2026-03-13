package com.example.lazuli_events.profile;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lazuli_events.MainActivity;
import com.example.lazuli_events.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.w3c.dom.Text;


/**
 * This is a profile Fragment that displays a user's personal information.
 * Editing user info will update this fragment with the proper details.
 */
public class ProfileFragment extends Fragment {

    ProfileController profileController;
    Profile profile;
    private static final String key = "sessionProfile";


    public ProfileFragment() {
    }

    /*
    public static ProfileFragment newInstance(Profile newProfile){
        ProfileFragment fragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(key, newProfile);
        fragment.setArguments(bundle);
        return fragment;
    }

     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        // get reference to the nav host (MainActivity)
        MainActivity mainActivity = (MainActivity) getActivity();

        // Fetch current user's session from MainActivity (specifically the nav)
        if (getArguments() == null){
            throw new NullPointerException("Issues reading profile. getArguments is null!");
        }

        Bundle profileBundle = getArguments();
        profile = (Profile) profileBundle.getSerializable(key);
        if (profile == null){
            throw new NullPointerException("Issue: profile equals null.");
        }

        // init edit button, which navigates to ProfileEditFragment
        Button editButton = rootView.findViewById(R.id.profile_edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navigate to the edit profile page
                assert mainActivity != null;
                mainActivity.navController.navigate(R.id.editProfileFragment, profileBundle);
            }
        });

        // init delete profile button, which displays a dialogue asking for confirmation
        TextView deleteProfileButton = rootView.findViewById(R.id.delete_profile_button);
        deleteProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // test
                Log.d("hi", "asdfasdfa");
            }
        });

        // TODO: create confirmation dialog box

//        MaterialAlertDialogBuilder deleteProfileDialog = new MaterialAlertDialogBuilder(rootView.getContext())
//                .setTitle("title")
//                .setMessage("are u sure")
//                .setIcon(R.drawable.ic_profile)
//                .setPositiveButton("asdfasdf") { _, _ ->
//                    Toast.makeText(mainActivity, "adfasdf", Toast.LENGTH_SHORT).show()
//        }


        // Display details in fragment (get id of each field in fragment_profile.xml)

        TextView profileNameView = rootView.findViewById(R.id.profile_name_textView);
        profileNameView.setText(profile.getName());

        TextView profileAddressView = rootView.findViewById(R.id.profile_address_textView);
        profileAddressView.setText("Default address"); //TODO: implement addresses (extra)

        TextView profilePhoneView = rootView.findViewById(R.id.profile_phone_textView);
        profilePhoneView.setText(profile.getPhone());

        TextView profileEmailView = rootView.findViewById(R.id.profile_email_textView);
        profileEmailView.setText(profile.getEmail());

        TextView profilePreferences = rootView.findViewById(R.id.user_preferences_textView);
        String pref = "Notification preference: "+profile.getNotifPref();
        profilePreferences.setText(pref);

        TextView profileCardName = rootView.findViewById(R.id.profile_card_text_name);
        profileCardName.setText(profile.getName());

        TextView profileCardLocation = rootView.findViewById(R.id.profile_card_text_location);
        profileCardLocation.setText("Edmonton"); //TODO: set to location (when adding location features)

        return rootView;

    }
}