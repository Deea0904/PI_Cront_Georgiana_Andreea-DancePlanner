package com.example.danceplanner.dto;

public class TimetableEntryDto {
    private String day;
    private String startTime;  // "18:00"
    private String endTime;    // "19:00"
    private String hall;
    private String coach;
    private String dancer;

    public TimetableEntryDto() {}

    public TimetableEntryDto(String day, String startTime, String endTime,
                             String hall, String coach, String dancer) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hall = hall;
        this.coach = coach;
        this.dancer = dancer;
    }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getHall() { return hall; }
    public void setHall(String hall) { this.hall = hall; }

    public String getCoach() { return coach; }
    public void setCoach(String coach) { this.coach = coach; }

    public String getDancer() { return dancer; }
    public void setDancer(String dancer) { this.dancer = dancer; }
}
