package com.example.socialnetworkforgamer.model;

public class Posts {
    public String uid;
    public String date;
    public String description;
    public String postimage;
    public String time;
    public String username;
    public String profileimage;
    public String hashtag;

    public Posts(){

    }

    public Posts(String uid, String date, String description, String postimage, String time, String username, String profileimage, String hashtag) {
        this.uid = uid;
        this.date = date;
        this.description = description;
        this.postimage = postimage;
        this.time = time;
        this.username = username;
        this.profileimage = profileimage;
        this.hashtag = hashtag;
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}
