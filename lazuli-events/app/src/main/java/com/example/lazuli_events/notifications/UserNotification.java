package com.example.lazuli_events.notifications;

public class UserNotification {
    // unique id for this notification document
    private String notificationId;

    // the user who should receive this notification
    private String recipientId;

    // the event related to this notification
    private String eventId;

    // short heading shown in the app
    private String title;

    // full notification message
    private String message;

    // type of notification, e.g. lottery_win or lottery_lose
    private String type;

    // time the notification was created
    private long timestamp;

    public UserNotification() {
        // required empty constructor for Firestore
    }

    public UserNotification(String notificationId, String recipientId, String eventId,
                            String title, String message, String type, long timestamp) {
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.eventId = eventId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}