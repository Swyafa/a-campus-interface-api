package com.smartcampus.model;

// Student ID: w2069246
// Student Name: Mohammed Sami Bari


import java.time.Instant;

public class SensorReading {
    public String id;
    public String sensorId;
    public double value;
    public String unit;
    public String timestamp;

    public SensorReading() {}

    public String getId()        { return id; }
    public String getSensorId()  { return sensorId; }
    public double getValue()     { return value; }
    public String getUnit()      { return unit; }
    public String getTimestamp() { return timestamp; }

    public void setId(String id)             { this.id = id; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }
    public void setValue(double value)       { this.value = value; }
    public void setUnit(String unit)         { this.unit = unit; }
    public void setTimestamp(String ts)      { this.timestamp = ts; }
}