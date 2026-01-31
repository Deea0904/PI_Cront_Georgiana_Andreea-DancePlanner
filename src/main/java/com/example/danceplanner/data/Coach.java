package com.example.danceplanner.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Coach {
    private Long id;
    private String name;

    public Coach() {
    }
   public Coach(Long id, String name){
        this.id = id;
        this.name = name;
    }

    public Long getId() {
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

//pt parola
    @JsonIgnore
    private String username;
    @JsonIgnore
    private String passwordHash;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    @Override
    public String toString(){
        return "Coach: "+id + " " + name ;
    }
}
