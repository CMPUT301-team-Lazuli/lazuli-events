package com.example.lazuli_events;

import java.util.ArrayList;

public class Event {
    private String eventId;
    private String title;
    private String description;
    private String location;
    private String organizerId;
    private int capacity;
    private ArrayList<String> waitlist;
    private ArrayList<String> accepted;

    public Event() {
        // required empty constructor for Firestore
    }

    public Event(String eventId, String title, String description, String location,
                 String organizerId, int capacity) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.organizerId = organizerId;
        this.capacity = capacity;
        this.waitlist = new ArrayList<>();
        this.accepted = new ArrayList<>();
    }

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public int getCapacity() {
        return capacity;
    }

    public ArrayList<String> getWaitlist() {
        return waitlist;
    }

    public ArrayList<String> getAccepted() {
        return accepted;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setWaitlist(ArrayList<String> waitlist) {
        this.waitlist = waitlist;
    }

    public void setAccepted(ArrayList<String> accepted) {
        this.accepted = accepted;
    }
}