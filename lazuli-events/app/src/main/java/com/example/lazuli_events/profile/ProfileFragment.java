package com.example.lazuli_events.profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.lazuli_events.MainActivity;
import com.example.lazuli_events.R;


/**
 * This is a profile Fragment that displays a user's personal information.
 * Editing user info will update this fragment with the proper details.
 */
public class ProfileFragment extends Fragment {

    ProfileController profileController;
    Profile profile;


    public ProfileFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        // get reference to the nav host (MainActivity)
        MainActivity mainActivity = (MainActivity) getActivity();


        // init edit button, which navigates to ProfileEditFragment
        Button editButton = rootView.findViewById(R.id.profile_edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navigate to the edit profile page
                assert mainActivity != null;
                mainActivity.navController.navigate(R.id.profileEditFragment);
            }
        });


        // TODO:
        // get user details from database
        // display details in fragment (get id of each field in fragment_user_profile.xml)

        return rootView;

    }
}