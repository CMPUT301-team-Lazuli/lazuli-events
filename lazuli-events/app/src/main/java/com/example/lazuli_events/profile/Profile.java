package com.example.lazuli_events.profile;

import java.util.ArrayList;

/**
 * This is a class representing a user profile. It stores a user's:
 * <li>profile data (name, email, phone number, deviceId)</li>
 * <li>event registration history, as a String ArrayList</li>
 * <li>notification preference</li>
 * These elements can be retrieved and updated, and the Profile can check if an event id is in
 * its event history.
 */
public class Profile {
    private String name;
    private String email;
    private String phone;
    private String deviceId;
    private String notifPref;
    private ArrayList<String> eventIds;

    public Profile(String name, String email, String phone, String deviceId,
                   String notifPref, ArrayList<String> eventIds){
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.deviceId = deviceId;
        this.notifPref = notifPref;
        this.eventIds = eventIds;
    }

    /**
     * Gets a profile's full name.
     * @return  the profile's name as a String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a profile's name to a new one.
     * @param name  the new name for the selected profile
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets a profile's email.
     * @return  the profile's email as a String
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets a profile's email to a new one.
     * @param email the new email for the selected profile
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets a profile's phone number.
     * @return  the profile's phone number as a String
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the profile's phone number to a new one.
     * @param phone the new phone number for the selected profile
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets a profile's device id.
     * @return  the profile's device id as a String
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the profile's device id to a new one.
     * @param deviceId  the new deviceId for the selected profile
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets a profile's notification preference.
     * @return the profile's notification preference
     */
    public String getNotifPref(){
        return notifPref;
    }

    /**
     * Sets the profile's device id to a new one, throws an error if new notification preference
     * is not one of the correct ones.
     * @param notifPref the new deviceId for the selected profile
     */
    public void setNotifPref(String notifPref){
        //update notification preference
        this.notifPref = notifPref;
    }

    /**
     * Gets a profile's event history.
     * @return  the profile's ArrayList of event IDs (as Strings)
     */
    public ArrayList<String> getEventIds(){
        return eventIds;
    }

    /**
     * Sets the profile's event history to a new one.
     * @param newEventIds   the new event history for a profile as an ArrayList of strings
     */
    public void setEventIds(ArrayList<String> newEventIds){
        this.eventIds = newEventIds;
    }

    /**
     * Adds a specific eventID to a profile's event history only if the eventID is not already in
     * the profile's event history.
     * @throws IllegalArgumentException if the event is already in a profile's history.
     * @param eventId the event ID to be added
     */
    public void addEventId(String eventId){
        //Assert event isn't already in event history
        if (eventIds.contains(eventId)){
            throw new IllegalArgumentException("Event already in event history.");
        }

        //Add event
        eventIds.add(eventId);
    }

    /** Checks if an eventId is in a profile's event history.
     * @param eventId: the event ID to be checked
     * @return boolean: true if the eventID is in its history, false if not
     */
    public boolean hasEventId(String eventId){
        return eventIds.contains(eventId);
    }

    /**
     * Removes a given event id from a Profile.
     * @param eventId   the event id to remove from the Profile
     * @throws IllegalArgumentException when the eventId to be deleted
     * is not in the Profile's eventIds
     */
    public void deleteEventId(String eventId){
        if (eventIds.contains(eventId)){
            eventIds.remove(eventId); //remove from profile
            //remove from db
        }
        else {
            throw new IllegalArgumentException();
        }
    }

}

