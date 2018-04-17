package com.example.zhaok.mychat;

/**
 * Created by zhaok on 4/16/2018.
 */

public class Chats
{
    private String userStatus;
    public Chats(){

    }

    public Chats(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
}
