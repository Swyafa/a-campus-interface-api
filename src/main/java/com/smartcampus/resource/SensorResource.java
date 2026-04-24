package com.smartcampus.resource;

// Student ID: w2069246
// Student Name: Mohammed Sami Bari


import com.smartcampus.exception.ErrorBody;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.dataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        List<Sensor> all = new ArrayList<Sensor>(dataStore.sensors.values());
        if (type != null && !type.trim().isEmpty()) {
            List<Sensor> filtered = new ArrayList<Sensor>();
            for (Sensor s : all) {
                if (type.equalsIgnoreCase(s.type)) filtered.add(s);
            }
            return filtered;
        }
        return all;
    }

    // this will validate if the roomId exists, returns 201 and location header
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.type == null || sensor.type.trim().isEmpty()) {
            return Response.status(400)
                    .entity(new ErrorBody(400, "Sensor type is required."))
                    .build();
        }
        if (sensor.roomId == null || sensor.roomId.trim().isEmpty()) {
            return Response.status(400)
                    .entity(new ErrorBody(400, "roomId is required."))
                    .build();
        }
        if (!dataStore.rooms.containsKey(sensor.roomId)) {
            throw new LinkedResourceNotFoundException(
                    "Cannot create sensor: room '" + sensor.roomId + "' does not exist."
            );
        }
        if (sensor.status == null || sensor.status.trim().isEmpty()) {
            sensor.status = "ACTIVE";
        }

        sensor.id = UUID.randomUUID().toString();
        dataStore.sensors.put(sensor.id, sensor);
        dataStore.readings.put(sensor.id, new ArrayList<com.smartcampus.model.SensorReading>());

        URI location = URI.create("http://localhost:8080/sensors/" + sensor.id);
        return Response.created(location).entity(sensor).build();  // 201 and  location header
    }


    @GET
    @Path("/{id}")
    public Response getSensor(@PathParam("id") String id) {
        Sensor sensor = dataStore.sensors.get(id);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorBody(404, "Sensor not found: " + id))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // the sub-resource locator
    @Path("/{id}/readings")
    public SensorReadingResource getReadingResource(@PathParam("id") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
