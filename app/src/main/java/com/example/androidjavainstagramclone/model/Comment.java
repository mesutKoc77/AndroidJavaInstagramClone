package com.example.androidjavainstagramclone.model;

import com.google.firebase.Timestamp;

public class Comment {
    public String userEmail;
    public String text;
    public Timestamp date;
    public String postId;

    public Comment() {
        // no-arg constructor for Firestore
    }

    public Comment(String userEmail, String text, Timestamp date, String postId) {
        this.userEmail = userEmail;
        this.text = text;
        this.date = date;
        this.postId = postId;
    }
}
