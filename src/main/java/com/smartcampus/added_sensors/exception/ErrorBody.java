package com.smartcampus.exception;

// Student ID: w2069246
// Student Name: Mohammed Sami Bari



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