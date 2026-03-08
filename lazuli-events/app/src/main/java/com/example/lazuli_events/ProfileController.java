package com.example.lazuli_events;

import android.util.Log;

/**
 * A profile controller that generates/retrieves device IDs to authenticate a user,
 * validates profile changes and saves them to the database,
 * and loads the most current profile data for the user.
 */
public class ProfileController {

    private String msg;


    public ProfileController(String string) {
        msg = string;
    }


    public void testPrint() {
        Log.d("key", this.msg);
    }
}
