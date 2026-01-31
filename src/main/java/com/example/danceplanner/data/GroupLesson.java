package com.example.danceplanner.data;

import java.time.LocalDateTime;

public class GroupLesson {
    private Long id;
    private Long groupId;
    private Long coachId;
    private Long danceHallId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public GroupLesson() {
    }

    public GroupLesson(Long groupId,
                       Long coachId,
                       Long danceHallId,
                       LocalDateTime startTime,
                       LocalDateTime endTime) {
        this.groupId = groupId;
        this.coachId = coachId;
        this.danceHallId = danceHallId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getCoachId() { return coachId; }
    public void setCoachId(Long coachId) { this.coachId = coachId; }

    public Long getDanceHallId() { return danceHallId; }
    public void setDanceHallId(Long danceHallId) { this.danceHallId = danceHallId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
