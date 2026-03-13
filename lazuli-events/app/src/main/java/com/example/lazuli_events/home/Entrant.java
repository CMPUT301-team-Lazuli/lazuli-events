package com.example.lazuli_events.home;

import com.example.lazuli_events.profile.Profile;

/**
 * Represents an entrant in the event system.
 * This class holds the user's profile and
 * their current status (accepted, cancelled, ...) for a specific event.
 */
public class Entrant {
    private Profile profile;
    private String status;
    private boolean isSelected; // tracks if the row is tapped

    /**
     * Constructs a new entrant with the specified profile and status.
     * @param profile The profile object containing user details.
     * @param status The current event status of the user (accepted, cancelled, waitlisted).
     */
    public Entrant(Profile profile, String status) {
        this.profile = profile;
        this.status = status;
        this.isSelected = false; // default not selected
    }

    /**
     * Retrieves the profile associated with this entrant.
     * @return The profile object containing the user's details.
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * Updates the profile associated with this entrant.
     * @param profile The new profile object to set.
     */
    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    /**
     * Retrieves the current event status of the entrant.
     * @return The current status such as accepted or waitlisted.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Updates the event status of the entrant.
     * @param status The new status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Checks if the entrant is currently selected (grey highlighted).
     * @return true if the entrant is selected, false otherwise.
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Sets the selection state of the entrant.
     * @param selected true to mark as selected, false to unselect.
     */
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}