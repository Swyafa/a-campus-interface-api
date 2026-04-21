package com.smartcampus;

// LOCATION: src/main/java/com/smartcampus/SmartCampusApplication.java

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // This class tells JAX-RS that all endpoints are rooted at /api/v1
    // It is required by the coursework specification
}
