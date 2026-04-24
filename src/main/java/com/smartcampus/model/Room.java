package com.smartcampus.model;

// Student ID: w2069246
// Student Name: Mohammed Sami Bari


public class Room {
    public String id;
    public String name;
    public int capacity;

    public Room() {}

    public Room(String id, String name, int capacity) {
        this.id       = id;
        this.name     = name;
        this.capacity = capacity;
    }

    public String getId()       { return id; }
    public String getName()     { return name; }
    public int    getCapacity() { return capacity; }

    public void setId(String id)          { this.id = id; }
    public void setName(String name)      { this.name = name; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}