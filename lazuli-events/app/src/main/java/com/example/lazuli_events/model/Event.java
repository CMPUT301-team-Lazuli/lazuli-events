package com.example.lazuli_events.model;

import java.util.ArrayList;

/**
 * Model class representing an event in the Lazuli Events application.
 */
public class Event {

    /** Unique ID of the event document. */
    private String id;

    /** User ID of the organizer who created the event. */
    private String organizerId;

    /** Display name of the event. */
    private String name;

    /** Description or summary of the event. */
    private String description;

    /** Physical or virtual location of the event. */
    private String location;

    /** Contact information for the organizer or event. */
    private String contact;

    /** Download URL of the event poster image. */
    private String posterUrl;

    /** Type or category of the event. */
    private String eventType;

    /** Audience restriction or attendance category for the event. */
    private String whoCanAttend;

    /** QR payload string associated with this event. */
    private String qrPayload;

    /** Event start time in milliseconds since epoch. */
    private Long eventStartMillis;

    /** Registration opening time in milliseconds since epoch. */
    private Long registrationStartMillis;

    /** Registration closing time in milliseconds since epoch. */
    private Long registrationEndMillis;

    /** Readable registration period text for easier display in Firestore/UI. */
    private String registrationPeriodText;

    /**
     * Maximum number of spots on the waitlist.
     *
     * <p>A {@code null} value means the waitlist is unlimited.</p>
     */
    private Long waitlistCap;

    /** Current number of users in the waitlist. */
    private int waitlistCount;

    /** List of entrant IDs currently on the waitlist. */
    private ArrayList<String> waitlist;

    /** Timestamp of when the event was created, in milliseconds since epoch. */
    private Long createdAt;

    /** Timestamp of the most recent event update, in milliseconds since epoch. */
    private Long updatedAt;

    /**
     * Creates a new empty event with an initialized waitlist.
     */
    public Event() {
        this.waitlist = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getWhoCanAttend() {
        return whoCanAttend;
    }

    public void setWhoCanAttend(String whoCanAttend) {
        this.whoCanAttend = whoCanAttend;
    }

    public String getQrPayload() {
        return qrPayload;
    }

    public void setQrPayload(String qrPayload) {
        this.qrPayload = qrPayload;
    }

    public Long getEventStartMillis() {
        return eventStartMillis;
    }

    public void setEventStartMillis(Long eventStartMillis) {
        this.eventStartMillis = eventStartMillis;
    }

    public Long getRegistrationStartMillis() {
        return registrationStartMillis;
    }

    public void setRegistrationStartMillis(Long registrationStartMillis) {
        this.registrationStartMillis = registrationStartMillis;
    }

    public Long getRegistrationEndMillis() {
        return registrationEndMillis;
    }

    public void setRegistrationEndMillis(Long registrationEndMillis) {
        this.registrationEndMillis = registrationEndMillis;
    }

    public String getRegistrationPeriodText() {
        return registrationPeriodText;
    }

    public void setRegistrationPeriodText(String registrationPeriodText) {
        this.registrationPeriodText = registrationPeriodText;
    }

    public Long getWaitlistCap() {
        return waitlistCap;
    }

    public void setWaitlistCap(Long waitlistCap) {
        this.waitlistCap = waitlistCap;
    }

    public int getWaitlistCount() {
        return waitlistCount;
    }

    public void setWaitlistCount(int waitlistCount) {
        this.waitlistCount = waitlistCount;
    }

    public ArrayList<String> getWaitlist() {
        if (waitlist == null) {
            waitlist = new ArrayList<>();
        }
        return waitlist;
    }

    public void setWaitlist(ArrayList<String> waitlist) {
        this.waitlist = (waitlist != null) ? waitlist : new ArrayList<>();
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isRegistrationOpen(long nowMillis) {
        return registrationStartMillis != null
                && registrationEndMillis != null
                && nowMillis >= registrationStartMillis
                && nowMillis <= registrationEndMillis;
    }
}