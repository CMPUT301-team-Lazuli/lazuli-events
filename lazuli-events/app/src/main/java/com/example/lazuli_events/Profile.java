package com.example.lazuli_events;

import java.util.ArrayList;

/**
 * This is a class representing a user profile.
 */
public class Profile {
    private String name;
    private String email;
    private String phone;
    private String deviceId;
    private String notifPref;
    private ArrayList<String> eventIds;
    private ProfileDB profileDB;

    public Profile(String name, String email, String phone, String deviceId,
                   String notifPref, ArrayList<String> eventIds, ProfileDB profileDB){
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.deviceId = deviceId;
        this.notifPref = notifPref;
        this.eventIds = eventIds;
        this.profileDB = profileDB;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getNotifPref(){
        return notifPref;
    }
    public void setNotifPref(String notifPref){
        this.notifPref = notifPref;
    }

    public ArrayList<String> getEventIds(){
        return eventIds;
    }
    public void addEventId(String eventId){
        eventIds.add(eventId);
    }

    public boolean hasEventId(String eventId){
        return eventIds.contains(eventId);
    }

    /**
     * Removes a given event id from a Profile.
     * @param eventId the event id to remove from the Profile
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

