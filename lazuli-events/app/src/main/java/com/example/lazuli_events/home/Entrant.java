package com.example.lazuli_events.home;

import com.example.lazuli_events.profile.Profile;

/**
 * a class that pairs a user's global Profile with their specific status for a single event
 * also tracks if the entrant is currently selected for deletion
 */
public class Entrant {
    private Profile profile;
    private String status;
    private boolean isSelected; // tracks if the row is tapped

    public Entrant(Profile profile, String status) {
        this.profile = profile;
        this.status = status;
        this.isSelected = false; // default not selected
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // getters and setters for selection state
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}