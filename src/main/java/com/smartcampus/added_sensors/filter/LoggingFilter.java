package com.smartcampus.filter;

// Student ID: w2069246
// Student Name: Mohammed Sami Bari

// LOCATION: src/main/java/com/smartcampus/filter/LoggingFilter.java

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class LoggingFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        LOG.info("[REQUEST]  " + req.getMethod() + " " + req.getUriInfo().getRequestUri());
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        LOG.info("[RESPONSE] " + req.getMethod() + " " + req.getUriInfo().getRequestUri()
                + " -> HTTP " + res.getStatus());
    }
}