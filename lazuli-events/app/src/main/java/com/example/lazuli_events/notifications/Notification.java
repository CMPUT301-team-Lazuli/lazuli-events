package com.example.lazuli_events.notifications;

import java.util.Date;

/**
 * represents a notification created by an organizer to be sent to entrants
 * stores the notification type, the message body, and the time it was sent
 */
public class Notification {

    private String type;
    private String message;
    private Date timestamp;

    /**
     * required empty public constructor for Firebase Firestore serialization
     */
    public Notification() {
    }

    /**
     * constructs a new Notification object
     * @param type The category of the notification (e.g., "General Update")
     * @param message the main text content written by the organizer
     * @param timestamp the exact time the notification was sent
     */
    public Notification(String type, String message, Date timestamp) {
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }

    // getters and setters

    /**
     * gets the notification type
     * @return the type as a String
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * gets the notification message
     * @return the message as a String
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * gets the timestamp of when the notification was created
     * @return the timestamp as a java.util.Date object
     */
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}