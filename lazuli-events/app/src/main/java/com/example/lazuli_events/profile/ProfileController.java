package com.example.lazuli_events.profile;

import android.util.Log;

import com.example.lazuli_events.FirebaseDB;

/**
 * A profile controller that generates/retrieves device IDs to authenticate a user,
 * validates profile changes and saves them to the database,
 * and loads the most current profile data for the user.
 */
public class ProfileController {

    private String msg;
    private Profile profile;
    private FirebaseDB firebaseDB;


    public ProfileController(String string, Profile profile) {
        this.msg = string;
        this.profile = profile;
    }


    public void testPrint() {
        Log.d("key", this.msg);
    }
}
