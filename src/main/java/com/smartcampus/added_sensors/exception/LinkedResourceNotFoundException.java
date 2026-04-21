package com.smartcampus.exception;

// LOCATION: src/main/java/com/smartcampus/exception/LinkedResourceNotFoundException.java

public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}