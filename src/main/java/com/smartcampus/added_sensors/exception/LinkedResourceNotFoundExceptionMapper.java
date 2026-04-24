package com.smartcampus.exception;

// Student ID: w2069246
// Student Name: Mohammed Sami Bari



import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        return Response
                .status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorBody(422, ex.getMessage()))
                .build();
    }
}