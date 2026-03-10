package com.example.lazuli_events;

import android.util.Log;

import com.example.lazuli_events.profile.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;
/**
 * A class for managing Profile-related database features. It creates,
 * reads, updates, and deletes user documents in Firestore as well as
 * converts raw Firestore records into user profiles.
 */
public class FirebaseDB {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference dbRefProfiles = FirebaseFirestore.getInstance().collection("profiles");
    private ArrayList<Profile> profiles;
    private ArrayList<String> emails;
    private ArrayList<String> events;

    /**
     * Creates a FirebaseDB object. In the constructor, various profiles are created based on Firestore's
     * sections and stored in an ArrayList.
     */
    public FirebaseDB(){
        //Profile section
        profiles = new ArrayList<Profile>();
        emails = new ArrayList<String>();
        dbRefProfiles.addSnapshotListener(((value, error) -> {
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

    //Events section

    /**
     * Get entrants for a specific event filtered by their status.
     */
    public void getEntrantsByStatus(String eventId, String status, final EntrantCallback callback) {
        ArrayList<String> entrantNames = new ArrayList<>();

        // Path: events -> [eventId] -> waitlist
        db.collection("events").document(eventId).collection("waitlist")
                .whereEqualTo("status", status.toLowerCase())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            entrantNames.add(document.getString("name"));
                        }
                        callback.onCallback(entrantNames);
                    } else {
                        Log.e("Firestore", "Error getting entrants: ", task.getException());
                    }
                });
    }

    /**
     * Permanently removes an entrant from an event's waitlist.
     */
    public void dropEntrantFromEvent(String eventId, String entrantName) {
        db.collection("events").document(eventId).collection("waitlist")
                .whereEqualTo("name", entrantName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                });
    }

    public interface EntrantCallback {
        void onCallback(ArrayList<String> list);
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

        //check email is unique
        if (emails.contains(profile.getEmail())){
            throw new IllegalArgumentException("Email already in use.");
        }

        //add to profiles
        profiles.add(profile);

        //add to database
        DocumentReference docRef = dbRefProfiles.document(profile.getEmail());
        docRef.set(profile);
    }

    /**
     * Removes a profile from the Firebase user documents
     * @param profile the profile to be removed
     * @throws IllegalArgumentException if the profile is not in the database
     */
    public void deleteProfileFromDB(Profile profile){
        //remove from profile array
        if (assertProfileInDatabase(profile)){
            profiles.remove(profile);
        }
        else {
            throw new IllegalArgumentException("Profile does not exist");
        }

        //remove from database
        DocumentReference docRef = dbRefProfiles.document(profile.getEmail());
        docRef.delete();
    }

    /**
     * Updates a profile's field in the database.
     * @param fieldToChange the Profile field you want to change (name, email, etc.)
     * @param newFieldStr the value to change the selected field to
     * @param profile the profile you want to update
     * @throws IllegalArgumentException if the profile is not in the database
     * @throws IllegalArgumentException if the new email is already in use.
     */
    public void updateProfileField(String fieldToChange, String newFieldStr, Profile profile){
        if (!assertProfileInDatabase(profile)){
            throw new IllegalArgumentException("Profile not in database.");
        }

        DocumentReference docRef = dbRefProfiles.document(profile.getEmail());

        switch (fieldToChange){
            case "name":
                profile.setName(newFieldStr);
                break;
            case "email":
                //assert email is unique
                if (emails.contains(profile.getEmail())){
                    throw new IllegalArgumentException("Email already in use (internal emails)");
                }
                profile.setEmail(newFieldStr);
                break;
            case "phoneNumber":
                profile.setPhone(newFieldStr);
                break;
            case "deviceId":
                profile.setDeviceId(newFieldStr);
                break;

            case "notifPref":
                profile.setNotifPref(newFieldStr);
                break;

            default:
                throw new IllegalArgumentException("Invalid field to change.");
        }
        docRef.set(profile);
    }

    public void overwriteProfile(Profile oldProfile, Profile newProfile){
        if (emails.contains(newProfile.getEmail())){
            throw new IllegalArgumentException("Email already in use");
        }
        else {
            DocumentReference docRef = dbRefProfiles.document(oldProfile.getEmail());
            docRef.set(newProfile);
        }
    }
    public void addEventIdToProfileHistory(String eventId, Profile profile){

    }

    public boolean assertProfileInDatabase(Profile profile){
        return (profiles.contains(profile));
    }


}
