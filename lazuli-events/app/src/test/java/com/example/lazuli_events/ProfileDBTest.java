package com.example.lazuli_events;

import org.junit.Test;

import java.util.ArrayList;

public class ProfileDBTest {
    private Profile mockProfile(String email){
        ArrayList<String> eventIds = new ArrayList<>();
        eventIds.add("A");
        eventIds.add("B");
        Profile mockProfile =  new Profile("Maya", email, "MPhone", "MDeviceId",
                "All", eventIds);
        return mockProfile;
    }

    /*
    private ProfileDB mockProfileDB(){
        ArrayList<String> emails = new ArrayList<>();
        emails.add("email1");
        emails.add("email2");

        Profile mockProfile = mockProfile("hello");
        return
    }
     */

    @Test
    public void testConstructor(){
        Profile mockProfile = mockProfile("hello");
    }
}
