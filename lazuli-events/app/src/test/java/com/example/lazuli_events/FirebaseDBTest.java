package com.example.lazuli_events;

import com.example.lazuli_events.profile.Profile;

import org.junit.Test;

import java.util.ArrayList;

public class FirebaseDBTest {
    private Profile mockProfile(){
        ArrayList<String> eventIds = new ArrayList<>();
        eventIds.add("A");
        eventIds.add("B");
        Profile mockProfile =  new Profile("Maya", "memail", "MPhone", "MDeviceId",
                "All", eventIds);
        return mockProfile;
    }


    private ArrayList<Profile> mockProfiles(){
        int amountOfProfiles = 5;
        ArrayList<String> eventIds = new ArrayList<String>();
        for (int i = 0; i < 5; i++){
            eventIds.add("event");
        }

        ArrayList<Profile> profiles = new ArrayList<Profile>();
        ArrayList<String> mockEmails = mockEmails();
        for (int i = 0; i < amountOfProfiles; i++){
            profiles.add(new Profile("name", mockEmails.get(i), "phone", "id",
                    "all", eventIds));
        }

        return profiles;
    }

    private ArrayList<String> mockEmails(){
        ArrayList<String> emails = new ArrayList<String>();
        emails.add("email1");
        emails.add("email2");
        emails.add("email3");
        emails.add("email4");
        emails.add("email5");
        return emails;
    }
    
    @Test
    public void testGetProfiles(){
        FirebaseDB firebaseDB = new FirebaseDB();
        firebaseDB.addProfileToDB(mockProfile());

        //get from firebase class
        ArrayList<Profile> profilesInFirebaseDB = firebaseDB.getProfiles();
        //get from db

        //check errors thrown
    }
    @Test
    public void testAddProfiles(){
        ArrayList<Profile> profiles = mockProfiles();
    }
}
