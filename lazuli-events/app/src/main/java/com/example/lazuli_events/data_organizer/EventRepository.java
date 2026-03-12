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

import java.util.HashMap;
import java.util.Map;

public class EventRepository {

    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface EventCallback {
        void onSuccess(Event event);
        void onError(Exception e);
    }

    public interface PosterUploadCallback {
        void onSuccess(String downloadUrl);
        void onError(Exception e);
    }

    public interface WaitlistStatusCallback {
        void onResult(boolean isInWaitlist);
        void onError(Exception e);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final StorageReference posterRoot =
            FirebaseStorage.getInstance().getReference("event_posters");

    /**
     * Save event only (no new poster upload).
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

        event.setUpdatedAt(System.currentTimeMillis());
        writeEventDocument(event, callback);
    }

    /**
     * Save event and optionally upload a new poster first.
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

        event.setUpdatedAt(System.currentTimeMillis());

        if (posterUri == null) {
            writeEventDocument(event, callback);
            return;
        }

        uploadPoster(event.getId(), posterUri, new PosterUploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                event.setPosterUrl(downloadUrl);
                writeEventDocument(event, callback);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * Upload poster image to Firebase Storage and return download URL.
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

    private void writeEventDocument(@NonNull Event event,
                                    @NonNull SimpleCallback callback) {
        db.collection("events")
                .document(event.getId())
                .set(event)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getEventById(@NonNull String eventId,
                             @NonNull EventCallback callback) {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Event event = snapshot.toObject(Event.class);
                    if (event == null) {
                        callback.onError(new Exception("Event not found"));
                        return;
                    }

                    // make sure model id is filled even if Firestore document id is not stored inside the document
                    if (event.getId() == null || event.getId().trim().isEmpty()) {
                        event.setId(snapshot.getId());
                    }

                    callback.onSuccess(event);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Check whether one entrant is already in an event waitlist.
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
     * Enforces BOTH:
     * 1) registration window
     * 2) optional waitlist cap
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

                    return null;
                }).addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Remove entrant from waitlist and decrease waitlist count.
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

                    return null;
                }).addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}