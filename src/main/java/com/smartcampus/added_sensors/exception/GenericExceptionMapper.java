package com.smartcampus.exception;

// Student ID: w2069246
// Student Name: Mohammed Sami Bari

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable ex) {
        // Never expose the stack trace in the response — security risk
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)   // 500
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorBody(500, "An unexpected error occurred."))
                .build();
    }
}
