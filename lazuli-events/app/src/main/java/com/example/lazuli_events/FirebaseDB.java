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
 * <p>This class manages the application's core data operations, primarily dealing with three
 * types of documents:</p>
 * <ul>
 * <li>{@link Profile} documents stored in the {@code profiles} collection.</li>
 * <li>{@link UserNotification} documents stored in the {@code notifications} collection.</li>
 * <li>{@link Event} documents stored in the {@code events} collection.</li>
 * </ul>
 *
 * <p>It also maintains a local in-memory list of profiles and their email addresses,
 * kept synchronized in real-time via a Firestore snapshot listener.</p>
 */
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

    /**
     * Callback interface for asynchronous operations that return a simple success or error string.
     */
    public interface SimpleCallback {
        /**
         * Invoked when the requested operation completes successfully.
         *
         * @param message A descriptive success message.
         */
        void onSuccess(String message);

        /**
         * Invoked when the requested operation fails.
         *
         * @param error A descriptive error message detailing the failure.
         */
        void onFailure(String error);
    }

    /**
     * Callback interface for asynchronous operations that retrieve lists of user notifications.
     */
    public interface NotificationsCallback {
        /**
         * Invoked when notifications are successfully fetched from the database.
         *
         * @param notifications The retrieved list of {@link UserNotification} objects.
         */
        void onSuccess(ArrayList<UserNotification> notifications);

        /**
         * Invoked when the notification retrieval operation fails.
         *
         * @param error A descriptive error message detailing the failure.
         */
        void onFailure(String error);
    }

    /**
     * Initializes the FirebaseDB instance and attaches a real-time snapshot listener
     * to the "profiles" collection in Firestore. This ensures the local {@code profiles}
     * and {@code emails} lists stay synchronized with the database.
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
     * Retrieves the locally cached list of profiles.
     *
     * @return The current {@link ArrayList} of {@link Profile} objects.
     */
    public ArrayList<Profile> getProfiles() {
        return profiles;
    }

    /**
     * Adds a new user profile to the Firestore database.
     *
     * @param profile The {@link Profile} to add.
     * @throws IllegalArgumentException If the provided email is already registered to an existing profile.
     */
    public void addProfileToDB(Profile profile) {
        if (emails.contains(profile.getEmail())) {
            throw new IllegalArgumentException("Email already in use.");
        }

        profiles.add(profile);
        dbRefProfiles.document(profile.getEmail()).set(profile);
    }

    /**
     * Deletes an existing user profile from the Firestore database.
     *
     * @param profile The {@link Profile} to delete.
     * @throws IllegalArgumentException If the profile does not exist in the database.
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
     * Updates a specific field within an existing user profile and synchronizes the change to Firestore.
     * If the email address is updated, the original document is deleted and a new document is created
     * under the new email address to act as the document ID.
     *
     * @param fieldToChange The name of the field to update (e.g., "name", "email", "phone").
     * @param newFieldStr   The new string value to set for the specified field.
     * @param profile       The target {@link Profile} to update.
     * @throws IllegalArgumentException If the profile does not exist, if an updated email is already in use,
     * or if the specified field name is unrecognized.
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
     * Completely replaces an existing profile with a new profile object in the database.
     * Handles document deletion and recreation if the email address (which serves as the document ID) changes.
     *
     * @param oldProfile The existing {@link Profile} to be replaced.
     * @param newProfile The new {@link Profile} data.
     * @throws IllegalArgumentException If the new profile's email address is already in use by a different user.
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
     * Appends an event ID to a user profile's history of interacted events using an atomic array union.
     *
     * @param eventId The ID of the event to add.
     * @param profile The target {@link Profile}.
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
     * Verifies if a given profile's email address currently exists in the local cache.
     *
     * @param profile The {@link Profile} to check.
     * @return {@code true} if the profile exists, {@code false} otherwise.
     */
    public boolean assertProfileInDatabase(Profile profile) {
        return emails.contains(profile.getEmail());
    }

    /**
     * Saves a new notification document to the Firestore database.
     *
     * @param notification The {@link UserNotification} object to save.
     * @param callback     A {@link SimpleCallback} to handle success or failure responses.
     */
    public void addNotification(UserNotification notification, SimpleCallback callback) {
        dbRefNotifications.document(notification.getNotificationId())
                .set(notification)
                .addOnSuccessListener(unused -> callback.onSuccess("Notification saved"))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Fetches all notifications addressed to a specific user, ordered by timestamp in descending order.
     *
     * @param recipientId The ID (email or deviceId) of the target user.
     * @param callback    A {@link NotificationsCallback} to return the parsed list of notifications.
     */
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

    /**
     * Deletes a specific notification from the Firestore database.
     *
     * @param notificationId The unique document ID of the notification to delete.
     * @param callback       A {@link SimpleCallback} to handle success or failure responses.
     */
    public void deleteNotification(String notificationId, SimpleCallback callback) {
        dbRefNotifications.document(notificationId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess("Notification deleted"))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Saves a new event document to the Firestore database. Generates a new unique document ID
     * if the event object does not currently have one assigned.
     *
     * @param event    The {@link Event} object to save.
     * @param callback A {@link SimpleCallback} to handle success or failure responses.
     */
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

    /**
     * Saves an event to Firestore and optionally uploads a corresponding poster image to Firebase Storage.
     * If an image URI is provided, the image is uploaded first, and its public download URL is
     * attached to the event object before saving it to the database.
     *
     * @param event    The {@link Event} object to save.
     * @param imageUri The local device {@link Uri} of the poster image (can be null).
     * @param callback A {@link SimpleCallback} to handle the success or failure of the upload/save sequence.
     */
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

    /**
     * Updates the registration period fields for a specific event in the database.
     *
     * @param eventId                 The document ID of the event to update.
     * @param registrationStartMillis The epoch time in milliseconds when registration opens.
     * @param registrationEndMillis   The epoch time in milliseconds when registration closes.
     * @param registrationPeriodText  A human-readable string representing the registration timeframe.
     * @param callback                A {@link SimpleCallback} to handle success or failure responses.
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
     * Generates and dispatches a notification to a specific user indicating they have won an event lottery.
     *
     * @param recipientId The target user's ID.
     * @param eventId     The associated event's ID.
     * @param eventTitle  The title of the event to display in the notification body.
     * @param callback    A {@link SimpleCallback} to handle success or failure responses.
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
     * Generates and dispatches a notification to a specific user indicating they did not win an event lottery.
     *
     * @param recipientId The target user's ID.
     * @param eventId     The associated event's ID.
     * @param eventTitle  The title of the event to display in the notification body.
     * @param callback    A {@link SimpleCallback} to handle success or failure responses.
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
     * Dispatches bulk lottery result notifications to groups of winning and losing users.
     * The provided callback will only return success once all individual notification
     * operations have successfully completed.
     *
     * @param eventId    The ID of the event holding the lottery.
     * @param eventTitle The title of the event holding the lottery.
     * @param winnerIds  A list of recipient IDs who won the lottery.
     * @param loserIds   A list of recipient IDs who lost the lottery.
     * @param callback   A {@link SimpleCallback} to track the aggregate success or failure of the batch.
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

    /**
     * Handles an entrant accepting an event invitation by moving their ID from the
     * chosen list to the accepted list in the database.
     *
     * @param eventId   The ID of the event the user is accepting.
     * @param entrantId The ID of the user accepting the invitation.
     * @param callback  A SimpleCallback to handle success or failure responses.
     */
    public void acceptEventInvitation(String eventId, String entrantId, SimpleCallback callback) {
        dbRefEvents.document(eventId)
                .update(
                        "chosenList", FieldValue.arrayRemove(entrantId),
                        "acceptedList", FieldValue.arrayUnion(entrantId)
                )
                .addOnSuccessListener(unused -> callback.onSuccess("Invitation accepted successfully."))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Handles an entrant declining an event invitation by moving their ID from the
     * chosen list to the declined list in the database.
     *
     * @param eventId   The ID of the event the user is declining.
     * @param entrantId The ID of the user declining the invitation.
     * @param callback  A SimpleCallback to handle success or failure responses.
     */
    public void declineEventInvitation(String eventId, String entrantId, SimpleCallback callback) {
        dbRefEvents.document(eventId)
                .update(
                        "chosenList", FieldValue.arrayRemove(entrantId),
                        "declinedList", FieldValue.arrayUnion(entrantId)
                )
                .addOnSuccessListener(unused -> callback.onSuccess("Invitation declined successfully."))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}