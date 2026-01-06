package com.example.dontjusteat.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class Slot implements Serializable {
    private static final long serialVersionUID = 1L;

    public Timestamp startTime;
    public Timestamp endTime;

    public Slot() {}

    public Slot(Timestamp startTime, Timestamp endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getDurationMillis() {
        if (startTime == null || endTime == null) return 0;
        return endTime.toDate().getTime() - startTime.toDate().getTime();
    }
}
