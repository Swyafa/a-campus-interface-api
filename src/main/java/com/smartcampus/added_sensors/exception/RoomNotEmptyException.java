package com.smartcampus.exception;

// LOCATION: src/main/java/com/smartcampus/exception/RoomNotEmptyException.java

public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String roomId) {
        super("Room " + roomId + " cannot be deleted because it still has sensors assigned to it.");
    }
}