package com.example.lazuli_events.data_organizer;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lazuli_events.model.Event;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository class responsible for managing {@link Event} data in Firebase Firestore
 * and event poster images in Firebase Storage.
 *
 * <p>This class provides functionality for:</p>
 * <ul>
 *     <li>Creating and updating events</li>
 *     <li>Uploading event posters</li>
 *     <li>Fetching event details by ID</li>
 *     <li>Checking whether a user is already in an event waitlist</li>
 *     <li>Joining and leaving event waitlists</li>
 * </ul>
 *
 * <p>Firestore structure used by this repository:</p>
 * <ul>
 *     <li><b>events</b> collection stores event documents</li>
 *     <li>Each event document may contain a <b>waitlist</b> subcollection</li>
 * </ul>
 *
 * <p>Poster images are stored in Firebase Storage under the
 * <code>event_posters</code> folder.</p>
 */
public class EventRepository {

    /**
     * Callback interface for operations that only need success or failure results.
     */
    public interface SimpleCallback {

        /**
         * Called when the operation completes successfully.
         */
        void onSuccess();

        /**
         * Called when the operation fails.
         *
         * @param e the exception describing the failure
         */
        void onError(Exception e);
    }

    /**
     * Callback interface for operations that return a single {@link Event}.
     */
    public interface EventCallback {

        /**
         * Called when the event is successfully retrieved and parsed.
         *
         * @param event the retrieved event
         */
        void onSuccess(Event event);

        /**
         * Called when the event retrieval or parsing fails.
         *
         * @param e the exception describing the failure
         */
        void onError(Exception e);
    }

    /**
     * Callback interface for poster image upload operations.
     */
    public interface PosterUploadCallback {

        /**
         * Called when the poster upload succeeds.
         *
         * @param downloadUrl the public download URL of the uploaded poster
         */
        void onSuccess(String downloadUrl);

        /**
         * Called when the poster upload fails.
         *
         * @param e the exception describing the failure
         */
        void onError(Exception e);
    }

    /**
     * Callback interface for checking whether a user is in an event's waitlist.
     */
    public interface WaitlistStatusCallback {

        /**
         * Returns the waitlist membership result.
         *
         * @param isInWaitlist {@code true} if the user is already in the waitlist,
         *                     {@code false} otherwise
         */
        void onResult(boolean isInWaitlist);

        /**
         * Called when the check fails.
         *
         * @param e the exception describing the failure
         */
        void onError(Exception e);
    }

    /** Firestore database instance used for event document operations. */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Root storage reference for event poster images. */
    private final StorageReference posterRoot =
            FirebaseStorage.getInstance().getReference("event_posters");

    /**
     * Saves an event to Firestore without uploading a poster image.
     *
     * <p>If the event is new, a new Firestore document ID is generated and assigned
     * to the event. Default values such as creation time, waitlist count, and QR payload
     * are also initialized if needed.</p>
     *
     * @param event the event to save
     * @param callback callback used to return success or failure
     */
    public void saveEvent(@NonNull Event event,
                          @NonNull SimpleCallback callback) {

        boolean isNew = event.getId() == null || event.getId().trim().isEmpty();

        if (isNew) {
            String id = db.collection("events").document().getId();
            event.setId(id);
            event.setCreatedAt(System.currentTimeMillis());

            if (event.getWaitlistCount() < 0) {
                event.setWaitlistCount(0);
            }
        }

        if (event.getWaitlist() == null) {
            event.setWaitlist(new ArrayList<>());
        }

        if (event.getQrPayload() == null || event.getQrPayload().trim().isEmpty()) {
            event.setQrPayload(buildQrPayload(event.getId()));
        }

        event.setUpdatedAt(System.currentTimeMillis());
        writeEventDocument(event, callback);
    }

    /**
     * Saves an event to Firestore and optionally uploads a poster image to Firebase Storage.
     *
     * <p>The event is first written to Firestore. If a poster URI is provided, the poster
     * is uploaded afterward, and the event document is updated with the resulting poster URL.</p>
     *
     * @param event the event to save
     * @param posterUri the URI of the poster image to upload; may be {@code null}
     * @param callback callback used to return success or failure
     */
    public void saveEvent(@NonNull Event event,
                          @Nullable Uri posterUri,
                          @NonNull SimpleCallback callback) {

        boolean isNew = event.getId() == null || event.getId().trim().isEmpty();

        if (isNew) {
            String id = db.collection("events").document().getId();
            event.setId(id);
            event.setCreatedAt(System.currentTimeMillis());

            if (event.getWaitlistCount() < 0) {
                event.setWaitlistCount(0);
            }
        }

        if (event.getWaitlist() == null) {
            event.setWaitlist(new ArrayList<>());
        }

        if (event.getQrPayload() == null || event.getQrPayload().trim().isEmpty()) {
            event.setQrPayload(buildQrPayload(event.getId()));
        }

        event.setUpdatedAt(System.currentTimeMillis());

        writeEventDocument(event, new SimpleCallback() {
            @Override
            public void onSuccess() {
                if (posterUri == null) {
                    callback.onSuccess();
                    return;
                }

                uploadPoster(event.getId(), posterUri, new PosterUploadCallback() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        event.setPosterUrl(downloadUrl);
                        event.setUpdatedAt(System.currentTimeMillis());

                        writeEventDocument(event, new SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess();
                            }

                            @Override
                            public void onError(Exception e) {
                                callback.onError(e);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(new Exception(
                                "Event was saved, but poster upload failed: " + e.getMessage()
                        ));
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * Uploads an event poster image to Firebase Storage.
     *
     * <p>The uploaded file is stored under the <code>event_posters</code> folder
     * using a filename based on the event ID and current timestamp.</p>
     *
     * @param eventId the ID of the event the poster belongs to
     * @param posterUri the URI of the poster image file
     * @param callback callback used to return the poster download URL or an error
     */
    public void uploadPoster(@NonNull String eventId,
                             @NonNull Uri posterUri,
                             @NonNull PosterUploadCallback callback) {

        String fileName = eventId + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = posterRoot.child(fileName);

        fileRef.putFile(posterUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri ->
                                        callback.onSuccess(downloadUri.toString()))
                                .addOnFailureListener(callback::onError)
                )
                .addOnFailureListener(callback::onError);
    }

    /**
     * Writes an event document to Firestore using the event's ID as the document ID.
     *
     * @param event the event to write
     * @param callback callback used to return success or failure
     */
    private void writeEventDocument(@NonNull Event event,
                                    @NonNull SimpleCallback callback) {
        db.collection("events")
                .document(event.getId())
                .set(event)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Builds the QR payload string for an event.
     *
     * @param eventId the event ID
     * @return a QR payload in the format {@code lazuli://event/{eventId}}
     */
    private String buildQrPayload(@NonNull String eventId) {
        return "lazuli://event/" + eventId;
    }

    /**
     * Retrieves an event from Firestore by its ID.
     *
     * <p>If the event document exists, its fields are manually extracted and mapped
     * into an {@link Event} object. If the document does not exist or parsing fails,
     * an error is returned through the callback.</p>
     *
     * @param eventId the ID of the event to retrieve
     * @param callback callback used to return the retrieved event or an error
     */
    public void getEventById(@NonNull String eventId,
                             @NonNull EventCallback callback) {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onError(new Exception("Event not found"));
                        return;
                    }

                    try {
                        Event event = new Event();

                        String id = snapshot.getString("id");
                        event.setId((id == null || id.trim().isEmpty()) ? snapshot.getId() : id);

                        event.setOrganizerId(snapshot.getString("organizerId"));
                        event.setName(snapshot.getString("name"));
                        event.setDescription(snapshot.getString("description"));
                        event.setLocation(snapshot.getString("location"));
                        event.setContact(snapshot.getString("contact"));
                        event.setPosterUrl(snapshot.getString("posterUrl"));

                        event.setEventType(snapshot.getString("eventType"));
                        event.setWhoCanAttend(snapshot.getString("whoCanAttend"));

                        String qrPayload = snapshot.getString("qrPayload");
                        if (qrPayload == null || qrPayload.trim().isEmpty()) {
                            qrPayload = buildQrPayload(event.getId());
                        }
                        event.setQrPayload(qrPayload);

                        event.setEventStartMillis(snapshot.getLong("eventStartMillis"));
                        event.setRegistrationStartMillis(snapshot.getLong("registrationStartMillis"));
                        event.setRegistrationEndMillis(snapshot.getLong("registrationEndMillis"));

                        event.setWaitlistCap(snapshot.getLong("waitlistCap"));

                        Long waitlistCountLong = snapshot.getLong("waitlistCount");
                        event.setWaitlistCount(waitlistCountLong == null ? 0 : waitlistCountLong.intValue());

                        event.setCreatedAt(snapshot.getLong("createdAt"));
                        event.setUpdatedAt(snapshot.getLong("updatedAt"));

                        ArrayList<String> fixedWaitlist = new ArrayList<>();
                        Object rawWaitlist = snapshot.get("waitlist");

                        if (rawWaitlist instanceof ArrayList<?>) {
                            for (Object item : (ArrayList<?>) rawWaitlist) {
                                if (item != null) {
                                    fixedWaitlist.add(String.valueOf(item));
                                }
                            }
                        }

                        event.setWaitlist(fixedWaitlist);

                        callback.onSuccess(event);

                    } catch (Exception e) {
                        callback.onError(new Exception("Failed to parse event: " + e.getMessage(), e));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Checks whether a user is currently in the waitlist of a specific event.
     *
     * <p>This method checks for the existence of a document in the event's
     * <code>waitlist</code> subcollection using the entrant ID as the document ID.</p>
     *
     * @param eventId the ID of the event
     * @param entrantId the ID of the entrant to check
     * @param callback callback used to return the result or an error
     */
    public void isUserInWaitlist(@NonNull String eventId,
                                 @NonNull String entrantId,
                                 @NonNull WaitlistStatusCallback callback) {

        db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(entrantId)
                .get()
                .addOnSuccessListener(snapshot -> callback.onResult(snapshot.exists()))
                .addOnFailureListener(callback::onError);
    }

    /**
     * Adds a user to the waitlist of an event.
     *
     * <p>This operation is performed inside a Firestore transaction to ensure that:</p>
     * <ul>
     *     <li>The event exists</li>
     *     <li>The registration window is currently open</li>
     *     <li>The user has not already joined the waitlist</li>
     *     <li>The waitlist is not full</li>
     * </ul>
     *
     * <p>If successful, a waitlist entry document is created, the waitlist count is
     * incremented, and the entrant ID is added to the event's waitlist array.</p>
     *
     * @param eventId the ID of the event
     * @param entrantId the ID of the user joining the waitlist
     * @param callback callback used to return success or failure
     */
    public void joinWaitlist(@NonNull String eventId,
                             @NonNull String entrantId,
                             @NonNull SimpleCallback callback) {

        DocumentReference eventRef = db.collection("events").document(eventId);
        DocumentReference waitlistRef = eventRef.collection("waitlist").document(entrantId);

        db.runTransaction(transaction -> {
                    DocumentSnapshot eventSnap = transaction.get(eventRef);

                    if (!eventSnap.exists()) {
                        throw new IllegalStateException("Event does not exist.");
                    }

                    Long start = eventSnap.getLong("registrationStartMillis");
                    Long end = eventSnap.getLong("registrationEndMillis");
                    Long capLong = eventSnap.getLong("waitlistCap");
                    Long countLong = eventSnap.getLong("waitlistCount");

                    long now = System.currentTimeMillis();
                    long count = countLong == null ? 0L : countLong;

                    if (start == null || end == null) {
                        throw new IllegalStateException("Registration period is not configured.");
                    }

                    if (now < start || now > end) {
                        throw new IllegalStateException("Registration is closed.");
                    }

                    DocumentSnapshot waitlistSnap = transaction.get(waitlistRef);
                    if (waitlistSnap.exists()) {
                        throw new IllegalStateException("You already joined this waitlist.");
                    }

                    if (capLong != null && capLong > 0 && count >= capLong) {
                        throw new IllegalStateException("Waitlist is full.");
                    }

                    Map<String, Object> waitlistEntry = new HashMap<>();
                    waitlistEntry.put("entrantId", entrantId);
                    waitlistEntry.put("joinedAt", FieldValue.serverTimestamp());
                    waitlistEntry.put("status", "waitlisted");

                    transaction.set(waitlistRef, waitlistEntry);
                    transaction.update(eventRef, "waitlistCount", count + 1);
                    transaction.update(eventRef, "waitlist", FieldValue.arrayUnion(String.valueOf(entrantId)));

                    return null;
                }).addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Removes a user from the waitlist of an event.
     *
     * <p>This operation is performed inside a Firestore transaction to ensure
     * consistency between the waitlist entry document, the waitlist count,
     * and the waitlist array stored in the event document.</p>
     *
     * @param eventId the ID of the event
     * @param entrantId the ID of the user leaving the waitlist
     * @param callback callback used to return success or failure
     */
    public void leaveWaitlist(@NonNull String eventId,
                              @NonNull String entrantId,
                              @NonNull SimpleCallback callback) {

        DocumentReference eventRef = db.collection("events").document(eventId);
        DocumentReference waitlistRef = eventRef.collection("waitlist").document(entrantId);

        db.runTransaction(transaction -> {
                    DocumentSnapshot waitlistSnap = transaction.get(waitlistRef);
                    if (!waitlistSnap.exists()) {
                        throw new IllegalStateException("You are not in this waitlist.");
                    }

                    DocumentSnapshot eventSnap = transaction.get(eventRef);
                    Long countLong = eventSnap.getLong("waitlistCount");
                    long count = countLong == null ? 0L : countLong;

                    transaction.delete(waitlistRef);
                    transaction.update(eventRef, "waitlistCount", Math.max(0, count - 1));
                    transaction.update(eventRef, "waitlist", FieldValue.arrayRemove(String.valueOf(entrantId)));

                    return null;
                }).addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}