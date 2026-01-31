package com.example.danceplanner.data;

public class DanceHall {
    private Long id;
    private String name;
    private int capacity;

    public DanceHall() {
    }
    public DanceHall(Long id, String name, int capacity){
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }
    public Long getId(){
        return id;
    }
    public void setId(long id){
        this.id = id;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public int getCapacity(){
        return capacity;
    }
    public void setCapacity(int capacity){
        this.capacity = capacity;
    }

    @Override
    public String toString(){
        return "Dance Hall: "+name+" Capacity: "+capacity;
    }
}
