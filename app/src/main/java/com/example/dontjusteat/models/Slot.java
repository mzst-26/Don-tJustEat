package com.example.dontjusteat.models;

import com.google.firebase.Timestamp;

public class Slot {
    public Timestamp startTime;
    public final Timestamp endTime;

    public Slot(Timestamp startTime, Timestamp endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
