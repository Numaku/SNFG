package com.example.socialnetworkforgamer.model;

public class FriendRequests {
    String requesttype;

    public FriendRequests(){

    }

    public FriendRequests(String requesttype) {
        this.requesttype = requesttype;
    }

    public String getRequesttype() {
        return requesttype;
    }

    public void setRequesttype(String requesttype) {
        this.requesttype = requesttype;
    }
}
