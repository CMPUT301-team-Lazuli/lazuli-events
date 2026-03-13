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

/**
 * Repository-style helper class for interacting with Firebase Firestore and Firebase Storage.
 *
 * <p>This class currently manages three main types of application data:</p>
 * <ul>
 *     <li>{@link Profile} documents stored in the {@code profiles} collection</li>
 *     <li>{@link UserNotification} documents stored in the {@code notifications} collection</li>
 *     <li>{@link Event} documents stored in the {@code events} collection</li>
 * </ul>
 *
 * <p>It also maintains local in-memory lists of profiles and profile email addresses
 * that are synchronized through a Firestore snapshot listener.</p>
 */
public class FirebaseDB {

    /** Firestore database instance used throughout the class. */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Reference to the Firestore collection that stores user profiles. */
    private final CollectionReference dbRefProfiles = db.collection("profiles");

    /** Reference to the Firestore collection that stores user notifications. */
    private final CollectionReference dbRefNotifications = db.collection("notifications");

    /** Reference to the Firestore collection that stores events. */
    private final CollectionReference dbRefEvents = db.collection("events");

    /** Firebase Storage instance used for poster image uploads. */
    private final FirebaseStorage storage =
            FirebaseStorage.getInstance("gs://lazuli-events.firebasestorage.app");
    /** Root Storage reference. */
    private final StorageReference storageRef = storage.getReference();

    /** Local cached list of profiles currently loaded from Firestore. */
    private final ArrayList<Profile> profiles;

    /** Local cached list of profile email addresses used for quick lookup and uniqueness checks. */
    private final ArrayList<String> emails;

    /**
     * Callback interface for simple asynchronous operations that return
     * either a success message or an error message.
     */
    public interface SimpleCallback {

        /**
         * Called when the operation completes successfully.
         *
         * @param message a success message describing the result
         */
        void onSuccess(String message);

        /**
         * Called when the operation fails.
         *
         * @param error an error message describing the failure
         */
        void onFailure(String error);
    }

    /**
     * Callback interface for fetching notification lists asynchronously.
     */
    public interface NotificationsCallback {

        /**
         * Called when notifications are successfully retrieved.
         *
         * @param notifications the list of retrieved notifications
         */
        void onSuccess(ArrayList<UserNotification> notifications);

        /**
         * Called when notification retrieval fails.
         *
         * @param error an error message describing the failure
         */
        void onFailure(String error);
    }

    /**
     * Creates a new {@code FirebaseDB} instance and attaches a snapshot listener
     * to the profiles collection.
     *
     * <p>The snapshot listener keeps the local {@code profiles} and {@code emails}
     * lists synchronized with the latest Firestore data.</p>
     */
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

    /**
     * Returns the locally cached list of profiles.
     *
     * @return the list of profiles currently loaded from Firestore
     */
    public ArrayList<Profile> getProfiles() {
        return profiles;
    }

    /**
     * Adds a new profile to Firestore.
     *
     * <p>This method first checks whether the profile email is already in use.
     * If it is, an exception is thrown. Otherwise, the profile is added to the
     * local list and written to Firestore using the email as the document ID.</p>
     *
     * @param profile the profile to add
     * @throws IllegalArgumentException if the email is already in use
     */
    public void addProfileToDB(Profile profile) {
        if (emails.contains(profile.getEmail())) {
            throw new IllegalArgumentException("Email already in use.");
        }

        profiles.add(profile);
        dbRefProfiles.document(profile.getEmail()).set(profile);
    }

    /**
     * Deletes a profile from Firestore.
     *
     * <p>If the profile does not exist in the local cache, an exception is thrown.</p>
     *
     * @param profile the profile to delete
     * @throws IllegalArgumentException if the profile does not exist
     */
    public void deleteProfileFromDB(Profile profile) {
        if (assertProfileInDatabase(profile)) {
            profiles.remove(profile);
        } else {
            throw new IllegalArgumentException("Profile does not exist");
        }

        dbRefProfiles.document(profile.getEmail()).delete();
    }

    /**
     * Updates a specific field in a profile and persists the change to Firestore.
     *
     * <p>Supported fields include:</p>
     * <ul>
     *     <li>{@code name}</li>
     *     <li>{@code email}</li>
     *     <li>{@code phoneNumber} or {@code phone}</li>
     *     <li>{@code deviceId}</li>
     *     <li>{@code notifPref}</li>
     * </ul>
     *
     * <p>If the email field changes, the old Firestore document is deleted and
     * a new document is created using the new email as the document ID.</p>
     *
     * @param fieldToChange the name of the field to update
     * @param newFieldStr the new value for the field
     * @param profile the profile to update
     * @throws IllegalArgumentException if the profile is not in the database,
     *                                  if the new email already exists,
     *                                  or if the field name is invalid
     */
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

    /**
     * Replaces an old profile with a new one in Firestore.
     *
     * <p>If the email changes, the old document is deleted and the new profile
     * is stored under the new email address.</p>
     *
     * @param oldProfile the original profile
     * @param newProfile the replacement profile
     * @throws IllegalArgumentException if the new email is already in use by another profile
     */
    public void overwriteProfile(Profile oldProfile, Profile newProfile) {
        if (!oldProfile.getEmail().equals(newProfile.getEmail()) && emails.contains(newProfile.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (!oldProfile.getEmail().equals(newProfile.getEmail())) {
            dbRefProfiles.document(oldProfile.getEmail()).delete();
        }

        dbRefProfiles.document(newProfile.getEmail()).set(newProfile);
    }

    /**
     * Adds an event ID to a profile's event history array in Firestore.
     *
     * <p>If the profile or event ID is invalid, the method does nothing.</p>
     *
     * @param eventId the event ID to add
     * @param profile the profile whose event history will be updated
     */
    public void addEventIdToProfileHistory(String eventId, Profile profile) {
        if (profile == null || eventId == null || eventId.trim().isEmpty()) {
            return;
        }

        dbRefProfiles.document(profile.getEmail())
                .update("eventIds", FieldValue.arrayUnion(eventId))
                .addOnFailureListener(e -> Log.e("Firestore", e.toString()));
    }

    /**
     * Checks whether a profile exists in the local cached database state.
     *
     * @param profile the profile to check
     * @return {@code true} if the profile email exists locally, {@code false} otherwise
     */
    public boolean assertProfileInDatabase(Profile profile) {
        return emails.contains(profile.getEmail());
    }

    /**
     * Saves a notification to Firestore.
     *
     * @param notification the notification to save
     * @param callback callback used to report success or failure
     */
    public void addNotification(UserNotification notification, SimpleCallback callback) {
        dbRefNotifications.document(notification.getNotificationId())
                .set(notification)
                .addOnSuccessListener(unused -> callback.onSuccess("Notification saved"))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Retrieves all notifications for a specific recipient, ordered by timestamp descending.
     *
     * @param recipientId the recipient user ID
     * @param callback callback used to return the list of notifications or an error
     */
    public void getNotificationsByRecipient(String recipientId, NotificationsCallback callback) {
        dbRefNotifications
                .whereEqualTo("recipientId", recipientId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<UserNotification> notifications = new ArrayList<>();
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        notifications.add(snapshot.toObject(UserNotification.class));
                    }
                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Marks a notification as read in Firestore.
     *
     * @param notificationId the ID of the notification to mark as read
     */
    public void markNotificationAsRead(String notificationId) {
        dbRefNotifications.document(notificationId).update("read", true);
    }

    /**
     * Saves an event to Firestore.
     *
     * <p>This stores all event fields, including registrationStartMillis,
     * registrationEndMillis, registrationPeriodText, and posterUrl if already set.</p>
     *
     * @param event the event to save
     * @param callback callback used to report success or failure
     */
    public void addEvent(Event event, SimpleCallback callback) {
        if (event == null) {
            callback.onFailure("Event is null.");
            return;
        }

        if (event.getId() == null || event.getId().trim().isEmpty()) {
            event.setId(dbRefEvents.document().getId());
        }
        String eventId = event.getId();

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

    /**
     * Uploads an event poster image to Firebase Storage, gets its download URL,
     * stores that URL in the event's posterUrl field, and then saves the event to Firestore.
     *
     * @param event the event to save
     * @param imageUri the local image Uri selected by the user
     * @param callback callback used to report success or failure
     */
    public void addEventWithPoster(Event event, Uri imageUri, SimpleCallback callback) {
        if (event == null) {
            callback.onFailure("Event is null.");
            return;
        }

        if (event.getId() == null || event.getId().trim().isEmpty()) {
            event.setId(dbRefEvents.document().getId());
        }
        String eventId = event.getId();

        long now = System.currentTimeMillis();
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(now);
        }
        event.setUpdatedAt(now);

        if (imageUri == null) {
            dbRefEvents.document(eventId)
                    .set(event)
                    .addOnSuccessListener(unused -> callback.onSuccess("Event saved without poster."))
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            return;
        }

        StorageReference posterRef = storageRef.child("event_posters/" + eventId + ".jpg");

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

                    dbRefEvents.document(eventId)
                            .set(event)
                            .addOnSuccessListener(unused ->
                                    callback.onSuccess("Event and poster saved successfully."))
                            .addOnFailureListener(e ->
                                    callback.onFailure("Poster uploaded, but event save failed: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Poster upload failed: " + e.getMessage()));
    }

    /**
     * Updates only the registration period fields of an existing event.
     *
     * @param eventId the event ID
     * @param registrationStartMillis registration start time
     * @param registrationEndMillis registration end time
     * @param registrationPeriodText readable registration period text
     * @param callback callback used to report success or failure
     */
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

    /**
     * Creates and sends a lottery-winning notification to a user.
     *
     * @param recipientId the user receiving the notification
     * @param eventId the related event ID
     * @param eventTitle the title of the related event
     * @param callback callback used to report success or failure
     */
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

    /**
     * Creates and sends a lottery-losing notification to a user.
     *
     * @param recipientId the user receiving the notification
     * @param eventId the related event ID
     * @param eventTitle the title of the related event
     * @param callback callback used to report success or failure
     */
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
     * Sends lottery result notifications to all winners and losers.
     *
     * <p>Each winner receives a lottery-win notification and each loser receives
     * a lottery-lose notification. The provided callback succeeds only after all
     * notifications are sent successfully. If any notification fails, the callback
     * fails immediately.</p>
     *
     * @param eventId the event ID associated with the lottery
     * @param eventTitle the event title associated with the lottery
     * @param winnerIds list of winner recipient IDs
     * @param loserIds list of loser recipient IDs
     * @param callback callback used to report overall success or failure
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
