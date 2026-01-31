package com.example.danceplanner.data;

import java.time.LocalDateTime;

public class CoachWorkingInterval {
    private Long id;
    private Long coachId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public CoachWorkingInterval() {
    }

    public CoachWorkingInterval(Long coachId, LocalDateTime startTime, LocalDateTime endTime) {
        this.coachId = coachId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCoachId() {
        return coachId;
    }

    public void setCoachId(Long coachId) {
        this.coachId = coachId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
