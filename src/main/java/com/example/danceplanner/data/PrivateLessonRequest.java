package com.example.danceplanner.data;

import java.util.ArrayList;
import java.util.List;

public class PrivateLessonRequest {
    private Long id;
    private Long dancerId;
    private Long coachId;
    private int numberOfLessons; // cate ore vrea
    private int lessonDurationMinutes; // de ex. 60 sau 45
    private List<AvailabilityInterval> availabilities = new ArrayList<>();

    public PrivateLessonRequest() {
    }

    public PrivateLessonRequest(Long dancerId,
            Long coachId,
            int numberOfLessons,
            int lessonDurationMinutes) {
        this.dancerId = dancerId;
        this.coachId = coachId;
        this.numberOfLessons = numberOfLessons;
        this.lessonDurationMinutes = lessonDurationMinutes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDancerId() {
        return dancerId;
    }

    public void setDancerId(Long dancerId) {
        this.dancerId = dancerId;
    }

    public Long getCoachId() {
        return coachId;
    }

    public void setCoachId(Long coachId) {
        this.coachId = coachId;
    }

    public int getNumberOfLessons() {
        return numberOfLessons;
    }

    public void setNumberOfLessons(int numberOfLessons) {
        this.numberOfLessons = numberOfLessons;
    }

    public int getLessonDurationMinutes() {
        return lessonDurationMinutes;
    }

    public void setLessonDurationMinutes(int lessonDurationMinutes) {
        this.lessonDurationMinutes = lessonDurationMinutes;
    }

    public List<AvailabilityInterval> getAvailabilities() {
        return availabilities;
    }

    public void setAvailabilities(List<AvailabilityInterval> availabilities) {
        this.availabilities = availabilities;
    }

    public void addAvailability(AvailabilityInterval interval) {
        this.availabilities.add(interval);
    }
}
