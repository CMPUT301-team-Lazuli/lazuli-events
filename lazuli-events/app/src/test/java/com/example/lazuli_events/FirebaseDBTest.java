package com.example.lazuli_events;

import android.util.Log;

import com.example.lazuli_events.profile.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.Test;

import java.util.ArrayList;

public class FirebaseDBTest {
    private Profile mockProfile(){
        ArrayList<String> eventIds = new ArrayList<>();
        eventIds.add("A");
        eventIds.add("B");
        Profile mockProfile =  new Profile("tester", "testemail", "123-4567", "device123",
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
    public void testAddDeleteProfiles(){
        //Add to database using firebaseDB class
        FirebaseDB firebaseDB = new FirebaseDB();
        Profile testProfile = mockProfile();
        firebaseDB.addProfileToDB(testProfile);

        //Check if the profile has been added to the actual firebase database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference colRef = db.collection("profiles");
        DocumentReference docRef = colRef.document(testProfile.getEmail());
        ArrayList<Profile> foundProfiles = new ArrayList<Profile>();


        /*
        colRef.addSnapshotListener((value, error) -> {
            if (error != null){
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()){
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value){
                    String name = snapshot.getString("name");
                    String email = snapshot.getString("email");
                    String phone = snapshot.getString("phone");
                    String devId = snapshot.getString("deviceId");
                    String notifPref = snapshot.getString("notifPref");
                    ArrayList<String> eventIds = (ArrayList<String>) snapshot.get("eventIds");

                    foundProfiles.add(new Profile(name, email, phone, devId, notifPref, eventIds));
                }
            }
        });

         */

    }

    @Test
    public void testGetProfiles(){

    }
}
