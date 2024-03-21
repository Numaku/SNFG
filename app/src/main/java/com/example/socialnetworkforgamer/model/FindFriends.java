package com.example.socialnetworkforgamer.model;

import com.google.firebase.database.PropertyName;

public class FindFriends {
    @PropertyName("profile image")
    public String profileimage;
    public String username;
    public String status;

    public FindFriends(){

    }

    public FindFriends(String profileimage, String username, String status) {
        this.profileimage = profileimage;
        this.username = username;
        this.status = status;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
