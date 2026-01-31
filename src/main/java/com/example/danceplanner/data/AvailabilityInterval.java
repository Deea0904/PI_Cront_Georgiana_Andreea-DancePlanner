package com.example.danceplanner.data;

import java.time.LocalDateTime;

public class AvailabilityInterval {
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public AvailabilityInterval() {
    }

    public AvailabilityInterval(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
