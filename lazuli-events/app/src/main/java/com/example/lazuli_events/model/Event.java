package com.example.lazuli_events.model;

import java.util.ArrayList;

public class Event {
    private String id;
    private String organizerId;
    private String name;
    private String description;
    private String location;
    private String contact;
    private String posterUrl;

    private Long eventStartMillis;
    private Long registrationStartMillis;
    private Long registrationEndMillis;

    // null = unlimited waitlist
    private Integer waitlistCap;

    // cached count for fast reads
    private int waitlistCount;

    // full list of entrant ids in the waitlist
    private ArrayList<String> waitlist;

    private Long createdAt;
    private Long updatedAt;

    public Event() {
        // Required empty constructor for Firestore
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

    public Integer getWaitlistCap() {
        return waitlistCap;
    }

    public void setWaitlistCap(Integer waitlistCap) {
        this.waitlistCap = waitlistCap;
    }

    public int getWaitlistCount() {
        return waitlistCount;
    }

    public void setWaitlistCount(int waitlistCount) {
        this.waitlistCount = waitlistCount;
    }

    public ArrayList<String> getWaitlist() {
        return waitlist;
    }

    public void setWaitlist(ArrayList<String> waitlist) {
        this.waitlist = waitlist;
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