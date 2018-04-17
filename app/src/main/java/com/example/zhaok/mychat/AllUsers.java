package com.example.zhaok.mychat;

/**
 * Created by zhaok on 4/8/2018.
 */

public class AllUsers
{
    public String userImage;
    public String userStatus;
    public String userName;
    public String userThumbImage;

    public AllUsers(){

    }

    public AllUsers(String userImage, String userStatus, String userName, String userThumbImage) {
        this.userImage = userImage;
        this.userStatus = userStatus;
        this.userName = userName;
        this.userThumbImage = userThumbImage;
    }

    public String getUserThumbImage() {
        return userThumbImage;
    }

    public void setUserThumbImage(String userThumbImage) {
        this.userThumbImage = userThumbImage;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
