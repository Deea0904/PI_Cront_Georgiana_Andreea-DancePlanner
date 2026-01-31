package com.example.danceplanner.dto;

public class UpdateDancerRequest {

    private String name;
    private String levelName;

    public UpdateDancerRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }
}
