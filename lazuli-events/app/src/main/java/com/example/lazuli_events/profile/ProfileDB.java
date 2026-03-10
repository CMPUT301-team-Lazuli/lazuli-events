package com.example.lazuli_events.profile;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
//TODO: figure out how we'll store things in the database

/**
 * A class for managing Profile-related database features. It creates,
 * reads, updates, and deletes user documents in Firestore as well as
 * converts raw Firestore records into user profiles.
 */
public class ProfileDB {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference dbRef = FirebaseFirestore.getInstance().collection("profiles");
    private ArrayList<Profile> profiles;
    private ArrayList<String> emails;

    /**
     * Creates a ProfileDB object. In the constructor, profiles are created based on Firestore's
     * "profile" section (the user documents) and stored in an ArrayList.
     */
    public ProfileDB(){
        profiles = new ArrayList<Profile>();
        emails = new ArrayList<String>();
        dbRef.addSnapshotListener(((value, error) -> {
            if (error != null){
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()){
                for (QueryDocumentSnapshot snapshot : value){
                    String name = snapshot.getString("name");
                    String email = snapshot.getString("email");
                    String notifPref = snapshot.getString("notifPref");
                    String phone = snapshot.getString("phone");
                    String devId = snapshot.getString("deviceId");
                    ArrayList<String> eventIds = (ArrayList<String>) snapshot.get("eventIds");
                    profiles.add(new Profile(name, email, phone, devId, notifPref, eventIds));
                    emails.add(email);
                }
            }
        }));
    }

    /**
     * Returns the created profiles as an ArrayList of Profile objects.
     *
     */
    public ArrayList<Profile> getProfiles(){
        return profiles;
    }

    /**
     * Adds a profile to the Firebase user documents. The profile must have an email
     * not currently used by another profile.
     * @param profile: the profile to be added
     */
    public void addProfileToDB(Profile profile){

        //DocumentReference docRef = dbRef.document(profile.getEmail());
        if (dbRef.document(profile.getEmail()).get().isSuccessful()){
            throw new IllegalArgumentException("Email already in use (db)");
        }

        //check email is unique
        /*
        if (emails.contains(profile.getEmail())){
            throw new IllegalArgumentException("Email already in use (internal emails)");
        }
        */
        //add to profiles
        profiles.add(profile);

        //add to database
        DocumentReference docRef = dbRef.document(profile.getEmail());
        docRef.set(profile);
    }

    /**
     * Removes a profile from the Firebase user documents
     * @param profile the profile to be removed
     * @throws IllegalArgumentException if the profile is not in the database
     */
    public void deleteProfileFromDB(Profile profile){
        //remove from profile array
        if (profiles.contains(profile)){
            profiles.remove(profile);
        }
        else {
            throw new IllegalArgumentException("Profile does not exist");
        }

        //remove from database
        DocumentReference docRef = dbRef.document(profile.getEmail());
        docRef.delete();
    }

    public void updateProfileName(String newName, Profile profile){

    }

    public void updateProfileEmail(String email, Profile profile){

    }

    public void updateProfilePhone(String phone, Profile profile){

    }

    public void updateProfileDeviceId(String deviceId, Profile profile){

    }

    public void updateProfileNotifPref(String notifPref, Profile profile){

    }

    public void addEventIdToProfileHistory(String eventId, Profile profile){

    }


}
