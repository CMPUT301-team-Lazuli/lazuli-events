package com.example.lazuli_events;

import com.example.lazuli_events.home.Entrant;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.example.lazuli_events.profile.Profile;

import org.junit.Test;
import java.util.ArrayList;

/**
 * Unit tests for the Entrant class.
 * Tests the installment and basic property retrieval of an entrant object.
 */
public class EntrantTest {

    /**
     * Tests the constructor of the entrant class to ensure fields
     * are correctly initialized and assigned.
     */
    @Test
    public void testEntrantConstructor() {
        // mock data for a user profile
        Profile mockProfile = new Profile("Lucas", "lucas@test.com", "123", "tester", "on", new ArrayList<>());
        String expectedStatus = "Accepted";

        // call the constructor to create an entrant object
        Entrant entrant = new Entrant(mockProfile, expectedStatus);

        // assert that the data was correctly assigned
        assertNotNull("Profile should not be null", entrant.getProfile());
        assertEquals("Profile name should be Lucas", "Lucas", entrant.getProfile().getName());
        assertEquals("Status should be Accepted", expectedStatus, entrant.getStatus());
        assertFalse("Default isSelected state should be false", entrant.isSelected());
    }
}