package com.smartcampus.exception;

// LOCATION: src/main/java/com/smartcampus/exception/SensorUnavailableException.java

public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String sensorId) {
        super("Sensor " + sensorId + " is currently in MAINTENANCE and cannot accept readings.");
    }
}