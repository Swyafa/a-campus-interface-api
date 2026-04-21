package com.smartcampus.resource;

// LOCATION: src/main/java/com/smartcampus/resource/rootResource.java
// REPLACE your existing rootResource.java with this

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.HashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class rootResource {

    @GET
    public Map<String, Object> discover() {

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("name", "Smart Campus Sensor API");
        response.put("version", "v1");
        response.put("description", "RESTful API for managing campus rooms, sensors, and sensor readings.");
        response.put("contact", "admin@smartcampus.ac.uk");

        // Resource map — tells clients where everything lives (HATEOAS principle)
        Map<String, String> resources = new HashMap<String, String>();
        resources.put("rooms",    "http://localhost:8080/rooms");
        resources.put("sensors",  "http://localhost:8080/sensors");
        resources.put("readings", "http://localhost:8080/sensors/{sensorId}/readings");
        response.put("resources", resources);

        return response;
    }
}
