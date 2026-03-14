package com.example.lazuli_events;

import static org.junit.Assert.assertNotNull;

import com.example.lazuli_events.notifications.Notification;

import org.junit.Test;

/**
 * Unit tests for the notification class.
 * Tests the basic installment of a Notification object.
 */
public class NotificationTest {

    /**
     * Tests the default constructor of the notification class to ensure
     * the object is successfully created in memory (for firebase later).
     */
    @Test
    public void testNotificationConstructor() {
        // call the empty constructor to create a notification object
        Notification notification = new Notification();

        // assert that the object was successfully created and is not null
        assertNotNull("Notification object should not be null", notification);
    }
}