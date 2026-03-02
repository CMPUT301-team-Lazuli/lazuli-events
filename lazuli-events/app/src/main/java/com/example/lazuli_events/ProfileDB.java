package com.example.lazuli_events;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
//TODO: figure out how we'll store things in the database

/**
 * A class for managing Profile-related database features. It creates,
 * reads, updates, and deletes user documents in Firestore as well as
 * converts raw Firestore records into user profiles.
 */
public class ProfileDB {
    private FirebaseFirestore db;
    private CollectionReference dbRef;
    private ArrayList<Profile> profiles;
    public ProfileDB(){
        //db = FirebaseFirestore.getInstance();
        //dbRef = FirebaseFirestore.collection("Profiles");
    }

    /**
     * Creates profiles from the Firestore user documents.
     */
    public void createProfilesFromDB(){
    }

    /**
     * Returns the array of created profiles.
     *
     */
    public ArrayList<Profile> getProfiles(){
        //if profiles aren't created, call createProfilesFromDB
        return profiles;
    }

    /**
     * Adds a profile to the Firebase user documents.
     * @param profile: the profile to be added
     */
    public void addProfileToDB(Profile profile){
        //add to profiles

        //add to database
    }

    /**
     * Removes a profile from the Firebase user documents
     * @param profile: the profile to be removed
     */
    public void deleteProfileFromDB(Profile profile){
    }

    /**
     * Changes a profile from the Firebase user documents
     * @param profile: the profile to be changed
     */
    public void updateProfile(Profile profile){

    }


}
