package com.example.zhaok.mychat;

/**
 * Created by zhaok on 4/16/2018.
 */

public class Requests
{
    private String userName;
    private String userStatus;
    private String userThumbImage;

    public Requests(){

    }

    public Requests(String userName, String userStatus, String userThumbImage) {
        this.userName = userName;
        this.userStatus = userStatus;
        this.userThumbImage = userThumbImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserThumbImage() {
        return userThumbImage;
    }

    public void setUserThumbImage(String userThumbImage) {
        this.userThumbImage = userThumbImage;
    }
}
