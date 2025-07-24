package com.example.androidjavainstagramclone.model;

public class Post {
    public String email;
    public String comment;
    public String downloadUrl;
    public String postId;

    public Post() {
        // required empty constructor
    }

    public Post(String email, String comment, String downloadUrl, String postId) {
        this.email = email;
        this.comment = comment;
        this.downloadUrl = downloadUrl;
        this.postId = postId;
    }
    @Override
    public String toString() {
        return "Username: " + email + ", Comment: " + comment + ", DownloadUrl: " + downloadUrl;
    }

}
