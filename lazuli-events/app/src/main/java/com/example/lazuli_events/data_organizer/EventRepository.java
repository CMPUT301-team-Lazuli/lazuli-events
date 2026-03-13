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
     * Save event only (no poster upload).
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

        event.setUpdatedAt(System.currentTimeMillis());
        writeEventDocument(event, callback);
    }

    /**
     * Save event and optionally upload poster.
     *
     * FIX:
     * 1. Save the event document first
     * 2. Upload poster second
     * 3. Update posterUrl in Firestore last
     *
     * This prevents "event does not exist" when poster upload fails.
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


        event.setUpdatedAt(System.currentTimeMillis());

        // Step 1: always save event first
        writeEventDocument(event, new SimpleCallback() {
            @Override
            public void onSuccess() {
                // No poster selected, done
                if (posterUri == null) {
                    callback.onSuccess();
                    return;
                }

                // Step 2: upload poster
                uploadPoster(event.getId(), posterUri, new PosterUploadCallback() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        // Step 3: update event with posterUrl
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
                    transaction.update(eventRef, "waitlist", FieldValue.arrayUnion(String.valueOf(entrantId)));

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
                    transaction.update(eventRef, "waitlist", FieldValue.arrayRemove(String.valueOf(entrantId)));

                    return null;
                }).addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}