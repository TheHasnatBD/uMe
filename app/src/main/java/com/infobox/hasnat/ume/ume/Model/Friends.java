package com.infobox.hasnat.ume.ume.Model;

public class Friends {

    private String user_name;
    private String user_thumb_image;
    private String date;

    public Friends() {
    }

    public Friends(String user_name, String user_thumb_image, String date) {
        this.user_name = user_name;
        this.user_thumb_image = user_thumb_image;
        this.date = date;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_thumb_image() {
        return user_thumb_image;
    }

    public void setUser_thumb_image(String user_thumb_image) {
        this.user_thumb_image = user_thumb_image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
