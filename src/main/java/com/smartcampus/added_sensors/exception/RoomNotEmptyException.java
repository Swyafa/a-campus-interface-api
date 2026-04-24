package com.smartcampus.exception;

// Student ID: w2069246
// Student Name: Mohammed Sami Bari



public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String roomId) {
        super("Room " + roomId + " cannot be deleted because it still has sensors assigned to it.");
    }
}