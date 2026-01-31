package com.example.danceplanner.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Dancer {
    private long id;
    private String name;
    private long levelId; // FK catre group_level
    private String levelName; // doar pentru afisare
    private Integer age;

    public Dancer() {
    }

    public Dancer(long id, String name, long levelId, int age) {
        this.id = id;
        this.name = name;
        this.levelId = levelId;
        this.age = age;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLevelId() {
        return levelId;
    }

    public void setLevelId(long levelId) {
        this.levelId = levelId;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public void setAge(Integer x) {
        this.age = x;
    }

    public Integer getAge() {
        return this.age;
    }

    // pt parola
    @JsonIgnore
    private String username;
    @JsonIgnore
    private String passwordHash;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return id + ". " + name + (levelName != null ? " (" + levelName + ")" : " [levelId=" + levelId + "]");
    }

}
