package com.infobox.hasnat.ume.ume.Model;

public class ProfileInfo {

    private String user_name, verified;
    private String user_image;
    private String user_status;
    private String user_thumb_image;

    public ProfileInfo() {
    }

    public ProfileInfo(String user_name, String verified, String user_image, String user_status, String user_thumb_image) {
        this.user_name = user_name;
        this.verified = verified;
        this.user_image = user_image;
        this.user_status = user_status;
        this.user_thumb_image = user_thumb_image;
    }




    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getUser_status() {
        return user_status;
    }

    public void setUser_status(String user_status) {
        this.user_status = user_status;
    }

    public String getUser_thumb_image() {
        return user_thumb_image;
    }

    public void setUser_thumb_image(String user_thumb_image) {
        this.user_thumb_image = user_thumb_image;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }
}
