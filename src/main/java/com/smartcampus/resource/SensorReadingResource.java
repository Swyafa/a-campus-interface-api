package com.smartcampus.resource;

// Student ID: w2069246
// Student Name: Mohammed Sami Bari

// LOCATION: src/main/java/com/smartcampus/resource/SensorReadingResource.java

import com.smartcampus.exception.ErrorBody;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.dataStore;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// No @Path here — path is set by the locator in SensorResource
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /sensors/{id}/readings
    @GET
    public Response getReadings() {
        Sensor sensor = dataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorBody(404, "Sensor not found: " + sensorId))
                    .build();
        }
        List<SensorReading> history = dataStore.readings.get(sensorId);
        if (history == null) history = new ArrayList<SensorReading>();
        return Response.ok(history).build();
    }

    // POST /sensors/{id}/readings
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorBody(404, "Sensor not found: " + sensorId))
                    .build();
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.status)) {
            throw new SensorUnavailableException(sensorId);  // 403
        }
        reading.id       = UUID.randomUUID().toString();
        reading.sensorId = sensorId;
        if (reading.timestamp == null || reading.timestamp.trim().isEmpty()) {
            reading.timestamp = Instant.now().toString();
        }
        List<SensorReading> list = dataStore.readings.get(sensorId);
        if (list == null) {
            list = new ArrayList<SensorReading>();
            dataStore.readings.put(sensorId, list);
        }
        list.add(reading);

        // Update parent sensor's currentValue — required by spec
        sensor.currentValue = reading.value;

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}