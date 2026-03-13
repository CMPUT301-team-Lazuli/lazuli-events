package com.example.lazuli_events.notifications;

import java.util.Date;

/**
 * Represents a notification sent by an organizer to entrants.
 * Contains the message details such as title, content, and the target audience.
 */
public class Notification {

    private String type;
    private String message;
    private Date timestamp;

    /**
     * Required empty public constructor for  Firestore.
     */
    public Notification() {
    }

    /**
     * Constructs a new Notification object.
     * @param type The category of the notification such as "General Update".
     * @param message The main text content written by the organizer.
     * @param timestamp The exact time the notification was sent.
     */
    public Notification(String type, String message, Date timestamp) {
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }

    // getters and setters
    /**
     * Gets the notification type.
     * @return The type as a string.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the notification type.
     * @param type The category of the notification to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the notification message.
     * @return the message as a string.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the notification message.
     * @param message The main text content to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the timestamp of when the notification was created.
     * @return the timestamp.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of the notification.
     * @param timestamp The exact time to set.
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}