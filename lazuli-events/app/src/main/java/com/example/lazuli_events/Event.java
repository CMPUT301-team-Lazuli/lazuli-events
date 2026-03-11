package com.example.lazuli_events;

import java.util.ArrayList;

public class Event {
    // unique id for the event
    private String eventId;

    // event name shown to users
    private String title;

    // event details/description
    private String description;

    // event price as text
    private String price;

    // list of entrant ids currently in the waitlist
    private ArrayList<String> waitlist;

    public Event() {
        // required empty constructor for Firestore
    }

    public Event(String eventId, String title, String description, String price, ArrayList<String> waitlist) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.waitlist = waitlist;
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

    public String getPrice() {
        return price;
    }

    public ArrayList<String> getWaitlist() {
        return waitlist;
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

    public void setPrice(String price) {
        this.price = price;
    }

    public void setWaitlist(ArrayList<String> waitlist) {
        this.waitlist = waitlist;
    }
}