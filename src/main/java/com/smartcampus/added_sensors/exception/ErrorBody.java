package com.smartcampus.exception;

// LOCATION: src/main/java/com/smartcampus/exception/ErrorBody.java

public class ErrorBody {
    private int status;
    private String error;

    public ErrorBody(int status, String error) {
        this.status = status;
        this.error  = error;
    }

    public int    getStatus() { return status; }
    public String getError()  { return error; }
}