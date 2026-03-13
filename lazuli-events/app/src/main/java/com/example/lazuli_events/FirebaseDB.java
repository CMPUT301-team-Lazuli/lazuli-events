package com.example.lazuli_events;

import android.net.Uri;
import android.util.Log;

import com.example.lazuli_events.model.Event;
import com.example.lazuli_events.notifications.UserNotification;
import com.example.lazuli_events.profile.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.UUID;

public class FirebaseDB {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbRefProfiles = db.collection("profiles");
    private final CollectionReference dbRefNotifications = db.collection("notifications");
    private final CollectionReference dbRefEvents = db.collection("events");

    private final FirebaseStorage storage =
            FirebaseStorage.getInstance("gs://lazuli-events.firebasestorage.app");
    private final StorageReference storageRef = storage.getReference();

    private final ArrayList<Profile> profiles;
    private final ArrayList<String> emails;

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

                    ArrayList<String> eventIds = new ArrayList<>();
                    Object rawEventIds = snapshot.get("eventIds");
                    if (rawEventIds instanceof ArrayList<?>) {
                        for (Object item : (ArrayList<?>) rawEventIds) {
                            if (item != null) {
                                eventIds.add(String.valueOf(item));
                            }
                        }
                    }

                    profiles.add(new Profile(name, email, phone, devId, notifPref, eventIds));
                    if (email != null) {
                        emails.add(email);
                    }
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
        dbRefProfiles.document(profile.getEmail()).set(profile);
    }

    public void deleteProfileFromDB(Profile profile) {
        if (assertProfileInDatabase(profile)) {
            profiles.remove(profile);
        } else {
            throw new IllegalArgumentException("Profile does not exist");
        }

        dbRefProfiles.document(profile.getEmail()).delete();
    }

    public void updateProfileField(String fieldToChange, String newFieldStr, Profile profile) {
        if (!assertProfileInDatabase(profile)) {
            throw new IllegalArgumentException("Profile not in database.");
        }

        String oldEmail = profile.getEmail();

        switch (fieldToChange) {
            case "name":
                profile.setName(newFieldStr);
                break;

            case "email":
                if (!oldEmail.equals(newFieldStr) && emails.contains(newFieldStr)) {
                    throw new IllegalArgumentException("Email already in use.");
                }
                profile.setEmail(newFieldStr);

                dbRefProfiles.document(oldEmail).delete();
                dbRefProfiles.document(newFieldStr).set(profile);

                emails.remove(oldEmail);
                emails.add(newFieldStr);
                return;

            case "phoneNumber":
            case "phone":
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

        dbRefProfiles.document(profile.getEmail()).set(profile);
    }

    public void overwriteProfile(Profile oldProfile, Profile newProfile) {
        if (!oldProfile.getEmail().equals(newProfile.getEmail()) && emails.contains(newProfile.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (!oldProfile.getEmail().equals(newProfile.getEmail())) {
            dbRefProfiles.document(oldProfile.getEmail()).delete();
        }

        dbRefProfiles.document(newProfile.getEmail()).set(newProfile);
    }

    public void addEventIdToProfileHistory(String eventId, Profile profile) {
        if (profile == null || eventId == null || eventId.trim().isEmpty()) {
            return;
        }

        dbRefProfiles.document(profile.getEmail())
                .update("eventIds", FieldValue.arrayUnion(eventId))
                .addOnFailureListener(e -> Log.e("Firestore", e.toString()));
    }

    public boolean assertProfileInDatabase(Profile profile) {
        return emails.contains(profile.getEmail());
    }

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
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        notifications.add(doc.toObject(UserNotification.class));
                    }
                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteNotification(String notificationId, SimpleCallback callback) {
        dbRefNotifications.document(notificationId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess("Notification deleted"))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void addEvent(Event event, SimpleCallback callback) {
        if (event == null) {
            callback.onFailure("Event is null.");
            return;
        }

        String eventId = event.getId();
        if (eventId == null || eventId.trim().isEmpty()) {
            eventId = dbRefEvents.document().getId();
            event.setId(eventId);
        }

        long now = System.currentTimeMillis();
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(now);
        }
        event.setUpdatedAt(now);

        dbRefEvents.document(eventId)
                .set(event)
                .addOnSuccessListener(unused -> callback.onSuccess("Event saved."))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void addEventWithPoster(Event event, Uri imageUri, SimpleCallback callback) {
        if (event == null) {
            callback.onFailure("Event is null.");
            return;
        }

        String eventId = event.getId();
        if (eventId == null || eventId.trim().isEmpty()) {
            eventId = dbRefEvents.document().getId();
            event.setId(eventId);
        }

        final String finalEventId = eventId;

        long now = System.currentTimeMillis();
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(now);
        }
        event.setUpdatedAt(now);

        if (imageUri == null) {
            dbRefEvents.document(finalEventId)
                    .set(event)
                    .addOnSuccessListener(unused -> callback.onSuccess("Event saved without poster."))
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            return;
        }

        StorageReference posterRef = storageRef.child("event_posters/" + finalEventId + ".jpg");

        posterRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Exception exception = task.getException();
                        if (exception != null) {
                            throw exception;
                        }
                    }
                    return posterRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    event.setPosterUrl(downloadUri.toString());

                    dbRefEvents.document(finalEventId)
                            .set(event)
                            .addOnSuccessListener(unused ->
                                    callback.onSuccess("Event and poster saved successfully."))
                            .addOnFailureListener(e ->
                                    callback.onFailure("Poster uploaded, but event save failed: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Poster upload failed: " + e.getMessage()));
    }

    public void updateEventRegistrationPeriod(String eventId,
                                              Long registrationStartMillis,
                                              Long registrationEndMillis,
                                              String registrationPeriodText,
                                              SimpleCallback callback) {

        if (eventId == null || eventId.trim().isEmpty()) {
            callback.onFailure("Event ID is required.");
            return;
        }

        if (registrationStartMillis == null || registrationEndMillis == null) {
            callback.onFailure("Registration start and end time are required.");
            return;
        }

        if (registrationStartMillis >= registrationEndMillis) {
            callback.onFailure("Registration open time must be before close time.");
            return;
        }

        dbRefEvents.document(eventId)
                .update(
                        "registrationStartMillis", registrationStartMillis,
                        "registrationEndMillis", registrationEndMillis,
                        "registrationPeriodText", registrationPeriodText,
                        "updatedAt", System.currentTimeMillis()
                )
                .addOnSuccessListener(unused -> callback.onSuccess("Registration period updated."))
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