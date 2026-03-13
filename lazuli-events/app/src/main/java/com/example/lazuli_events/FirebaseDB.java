package com.example.lazuli_events;

import android.util.Log;

import com.example.lazuli_events.notifications.UserNotification;
import com.example.lazuli_events.profile.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Handles app data in Firestore:
 * profiles and notifications only.
 */
public class FirebaseDB {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final CollectionReference dbRefProfiles = db.collection("profiles");
    private final CollectionReference dbRefNotifications = db.collection("notifications");

    private final ArrayList<Profile> profiles;
    private final ArrayList<String> emails;

    // simple callback for success/failure messages
    public interface SimpleCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    // callback used when loading a list of notifications
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

    // ---------------- NOTIFICATIONS ----------------

    // save one notification to the notifications collection
    public void addNotification(UserNotification notification, SimpleCallback callback) {
        dbRefNotifications.document(notification.getNotificationId())
                .set(notification)
                .addOnSuccessListener(unused -> callback.onSuccess("Notification saved"))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // load all notifications for one user, newest first
    public void getNotificationsForUser(String recipientId, NotificationsCallback callback) {
        dbRefNotifications
                .whereEqualTo("recipientId", recipientId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<UserNotification> notifications = new ArrayList<>();

                    // convert each Firestore document into a UserNotification object
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        UserNotification notification = snapshot.toObject(UserNotification.class);
                        notifications.add(notification);
                    }

                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // create and save a "winner" lottery notification
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

    // create and save a "loser" lottery notification
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
     * send winner notifications to winners and loser notifications to losers
     */
    public void sendLotteryResults(String eventId,
                                   String eventTitle,
                                   ArrayList<String> winnerIds,
                                   ArrayList<String> loserIds,
                                   SimpleCallback callback) {

        // total number of notifications that need to be sent
        int totalToSend = winnerIds.size() + loserIds.size();

        // stop if there are no results to send
        if (totalToSend == 0) {
            callback.onFailure("No lottery results to send.");
            return;
        }

        // track completed sends and whether any failure happened
        final int[] completed = {0};
        final boolean[] failed = {false};

        SimpleCallback innerCallback = new SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                completed[0]++;

                // when all notifications are sent successfully, return success once
                if (completed[0] == totalToSend && !failed[0]) {
                    callback.onSuccess("Lottery notifications sent.");
                }
            }

            @Override
            public void onFailure(String error) {
                // stop on the first failure
                if (!failed[0]) {
                    failed[0] = true;
                    callback.onFailure(error);
                }
            }
        };

        // send notifications to winners
        for (String winnerId : winnerIds) {
            sendLotteryWinNotification(winnerId, eventId, eventTitle, innerCallback);
        }

        // send notifications to losers
        for (String loserId : loserIds) {
            sendLotteryLoseNotification(loserId, eventId, eventTitle, innerCallback);
        }
    }}