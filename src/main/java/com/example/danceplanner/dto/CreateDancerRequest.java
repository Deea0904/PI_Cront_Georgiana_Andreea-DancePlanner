package com.example.danceplanner.dto;

public class CreateDancerRequest {

    private String name;
    private String levelName;
    private Integer age;

    // constructor gol
    public CreateDancerRequest() {
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }
}
