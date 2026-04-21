package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/api/v1")
public class rootResource {

    @GET
    public String hello() {
        return "Smart Campus API Running";
    }
}