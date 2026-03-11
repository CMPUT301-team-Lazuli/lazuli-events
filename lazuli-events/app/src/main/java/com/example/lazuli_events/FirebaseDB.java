package com.example.lazuli_events;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.lazuli_events.Event;
import com.example.lazuli_events.notifications.UserNotification;
import com.example.lazuli_events.profile.Profile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Handles app data in Firestore:
 * profiles, events, notifications.
 */
public class FirebaseDB {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference dbRefProfiles = db.collection("profiles");
    private CollectionReference dbRefEvents = db.collection("events");
    private CollectionReference dbRefNotifications = db.collection("notifications");

    private ArrayList<Profile> profiles;
    private ArrayList<String> emails;

    public interface SimpleCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface NotificationsCallback {
        void onSuccess(ArrayList<UserNotification> notifications);
        void onFailure(String error);
    }

    public FirebaseDB() {
        profiles = new ArrayList<>();
        emails = new ArrayList<>();

        dbRefProfiles.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
                return;
            }

            profiles.clear();
            emails.clear();

            if (value != null && !value.isEmpty()) {
                for (QueryDocumentSnapshot snapshot : value) {
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
        });
    }

    public ArrayList<Profile> getProfiles() {
        return profiles;
    }

    public void addProfileToDB(Profile profile) {
        if (emails.contains(profile.getEmail())) {
            throw new IllegalArgumentException("Email already in use.");
        }

        profiles.add(profile);
        DocumentReference docRef = dbRefProfiles.document(profile.getEmail());
        docRef.set(profile);
    }

    public void deleteProfileFromDB(Profile profile) {
        if (assertProfileInDatabase(profile)) {
            profiles.remove(profile);
        } else {
            throw new IllegalArgumentException("Profile does not exist");
        }

        DocumentReference docRef = dbRefProfiles.document(profile.getEmail());
        docRef.delete();
    }

    public void updateProfileField(String fieldToChange, String newFieldStr, Profile profile) {
        if (!assertProfileInDatabase(profile)) {
            throw new IllegalArgumentException("Profile not in database.");
        }

        DocumentReference docRef = dbRefProfiles.document(profile.getEmail());

        switch (fieldToChange) {
            case "name":
                profile.setName(newFieldStr);
                break;
            case "email":
                if (emails.contains(newFieldStr)) {
                    throw new IllegalArgumentException("Email already in use.");
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

    public void overwriteProfile(Profile oldProfile, Profile newProfile) {
        if (!oldProfile.getEmail().equals(newProfile.getEmail()) && emails.contains(newProfile.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        } else {
            DocumentReference docRef = dbRefProfiles.document(oldProfile.getEmail());
            docRef.set(newProfile);
        }
    }

    public boolean assertProfileInDatabase(Profile profile) {
        return emails.contains(profile.getEmail());
    }

    // ---------------- EVENTS ----------------

    public void addEventToDB(Event event) {
        DocumentReference docRef = dbRefEvents.document(event.getEventId());
        docRef.set(event);
    }

    public void getEventById(String eventId,
                             OnSuccessListener<DocumentSnapshot> onSuccess,
                             OnFailureListener onFailure) {
        dbRefEvents.document(eventId)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void joinWaitlist(String eventId, String entrantId, SimpleCallback callback) {
        DocumentReference eventRef = dbRefEvents.document(eventId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                callback.onFailure("Event does not exist.");
                return;
            }

            ArrayList<String> waitlist = (ArrayList<String>) documentSnapshot.get("waitlist");
            if (waitlist != null && waitlist.contains(entrantId)) {
                callback.onFailure("You already joined this waitlist.");
                return;
            }

            eventRef.update("waitlist", FieldValue.arrayUnion(entrantId))
                    .addOnSuccessListener(unused -> callback.onSuccess("Joined waitlist successfully."))
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void leaveWaitlist(String eventId, String entrantId, SimpleCallback callback) {
        DocumentReference eventRef = dbRefEvents.document(eventId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                callback.onFailure("Event does not exist.");
                return;
            }

            ArrayList<String> waitlist = (ArrayList<String>) documentSnapshot.get("waitlist");
            if (waitlist == null || !waitlist.contains(entrantId)) {
                callback.onFailure("You are not in this waitlist.");
                return;
            }

            eventRef.update("waitlist", FieldValue.arrayRemove(entrantId))
                    .addOnSuccessListener(unused -> callback.onSuccess("Left waitlist successfully."))
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ---------------- NOTIFICATIONS ----------------

    public void addNotification(UserNotification notification, SimpleCallback callback) {
        dbRefNotifications.document(notification.getNotificationId())
                .set(notification)
                .addOnSuccessListener(unused -> callback.onSuccess("Notification saved"))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getNotificationsForUser(String recipientId, NotificationsCallback callback) {
        dbRefNotifications
                .whereEqualTo("recipientId", recipientId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<UserNotification> notifications = new ArrayList<>();

                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        UserNotification notification = snapshot.toObject(UserNotification.class);
                        notifications.add(notification);
                    }

                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void sendLotteryWinNotification(String recipientId, String eventId, String eventTitle,
                                           SimpleCallback callback) {
        String notificationId = UUID.randomUUID().toString();

        UserNotification notification = new UserNotification(
                notificationId,
                recipientId,
                eventId,
                "Lottery Result",
                "You were selected for " + eventTitle + ".",
                "lottery_win",
                System.currentTimeMillis()
        );

        addNotification(notification, callback);
    }

    public void sendLotteryLoseNotification(String recipientId, String eventId, String eventTitle,
                                            SimpleCallback callback) {
        String notificationId = UUID.randomUUID().toString();

        UserNotification notification = new UserNotification(
                notificationId,
                recipientId,
                eventId,
                "Lottery Result",
                "You were not selected for " + eventTitle + ".",
                "lottery_lose",
                System.currentTimeMillis()
        );

        addNotification(notification, callback);
    }

    /**
     * Call this when lottery results are finalized.
     * winners get "lottery_win", everyone else gets "lottery_lose".
     */
    public void sendLotteryResults(String eventId,
                                   String eventTitle,
                                   ArrayList<String> winnerIds,
                                   ArrayList<String> loserIds,
                                   SimpleCallback callback) {
        int totalToSend = winnerIds.size() + loserIds.size();

        if (totalToSend == 0) {
            callback.onFailure("No lottery results to send.");
            return;
        }

        final int[] completed = {0};
        final boolean[] failed = {false};

        SimpleCallback innerCallback = new SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                completed[0]++;
                if (completed[0] == totalToSend && !failed[0]) {
                    callback.onSuccess("Lottery notifications sent.");
                }
            }

            @Override
            public void onFailure(String error) {
                if (!failed[0]) {
                    failed[0] = true;
                    callback.onFailure(error);
                }
            }
        };

        for (String winnerId : winnerIds) {
            sendLotteryWinNotification(winnerId, eventId, eventTitle, innerCallback);
        }

        for (String loserId : loserIds) {
            sendLotteryLoseNotification(loserId, eventId, eventTitle, innerCallback);
        }
    }
}