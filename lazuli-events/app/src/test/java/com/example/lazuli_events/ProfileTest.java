package com.example.lazuli_events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;

public class ProfileTest {

    private Profile mockProfile(){
        ArrayList<String> eventIds = new ArrayList<>();
        eventIds.add("A");
        eventIds.add("B");
        Profile mockProfile =  new Profile("Maya", "MEmail", "MPhone", "MDeviceId",
                "All", eventIds);
        return mockProfile;
    }

    @Test
    public void testGetters(){
        Profile mockProfile = mockProfile();
        ArrayList<String> correctEventIds = new ArrayList<String>();
        correctEventIds.add("A");
        correctEventIds.add("B");
        assertEquals("Maya", mockProfile.getName());
        assertEquals("MEmail", mockProfile.getEmail());
        assertEquals("MPhone", mockProfile.getPhone());
        assertEquals("MDeviceId", mockProfile.getDeviceId());
        assertEquals("All", mockProfile.getNotifPref());
        assertEquals(correctEventIds, mockProfile.getEventIds());
    }

    @Test
    public void testSetters(){
        Profile mockProfile = mockProfile();
        ArrayList<String> newEventIds = new ArrayList<>();
        newEventIds.add("D");
        newEventIds.add("E");
        newEventIds.add("F");

        mockProfile.setName("Maya2");
        assertEquals("Maya2", mockProfile.getName());

        mockProfile.setEmail("NewEmail");
        assertEquals("NewEmail", mockProfile.getEmail());

        mockProfile.setPhone("NewPhone");
        assertEquals("NewPhone", mockProfile.getPhone());

        mockProfile.setDeviceId("NewDeviceID");
        assertEquals("NewDeviceID", mockProfile.getDeviceId());

        mockProfile.setNotifPref("Essential");
        assertEquals("Essential", mockProfile.getNotifPref());

        mockProfile.setEventIds(newEventIds);
        assertEquals(newEventIds, mockProfile.getEventIds());
    }

    @Test
    public void testEventHistoryAddDeleteHas(){
        Profile mockProfile = mockProfile();
        ArrayList<String> eventIds = new ArrayList<>();
        eventIds.add("A");
        eventIds.add("B");

        //Adding to event history
        eventIds.add("C");
        mockProfile.addEventId("C");
        assertEquals(eventIds, mockProfile.getEventIds());
        assertThrows(IllegalArgumentException.class, () -> {
            mockProfile.addEventId("C");
        });

        //Deleting from event history
        eventIds.remove("C");
        mockProfile.deleteEventId("C");
        assertEquals(eventIds, mockProfile.getEventIds());
        assertThrows(IllegalArgumentException.class, () -> {
            mockProfile.deleteEventId("C");
        });

        //Checking if an event is in a profile's history
        assertTrue(mockProfile.hasEventId("A"));
        assertFalse(mockProfile.hasEventId("Not in the list"));

    }

}
