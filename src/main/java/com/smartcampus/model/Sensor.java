package com.smartcampus.model;

// Student ID: w2069246
// Student Name: Mohammed Sami Bari


public class Sensor {
    public String id;
    public String type;         // e.g. CO2, TEMPERATURE, HUMIDITY
    public String status;       // ACTIVE, INACTIVE, MAINTENANCE
    public double currentValue;
    public String roomId;

    public Sensor() {}

    public String getId()           { return id; }
    public String getType()         { return type; }
    public String getStatus()       { return status; }
    public double getCurrentValue() { return currentValue; }
    public String getRoomId()       { return roomId; }

    public void setId(String id)           { this.id = id; }
    public void setType(String type)       { this.type = type; }
    public void setStatus(String status)   { this.status = status; }
    public void setCurrentValue(double v)  { this.currentValue = v; }
    public void setRoomId(String roomId)   { this.roomId = roomId; }
}