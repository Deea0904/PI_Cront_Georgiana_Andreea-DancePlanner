package com.example.danceplanner.data;

public class Group {
    private long id;
    private String name;
    private String description; // poate fi null

    public Group() {
    }
    public Group(long id, String name, String description){
        this.id=id; this.name=name; this.description=description;
    }
    public Group(String name){ this(0, name, null); }

    public long getId(){ return id; }
    public void setId(long id){ this.id = id; }

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }

    public String getDescription(){ return description; }
    public void setDescription(String description){ this.description = description; }

    @Override public String toString(){
        return id + ". " + name + (description!=null && !description.isBlank() ? " — " + description : "");
    }
}
