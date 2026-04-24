package com.smartcampus.resource;

// Student ID: w2069246
// Student Name: Mohammed Sami Bari

// LOCATION: src/main/java/com/smartcampus/resource/RoomResource.java

import com.smartcampus.exception.ErrorBody;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.dataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {
    
    @GET
    public List<Room> getRooms() {
        return new ArrayList<Room>(dataStore.rooms.values());
    }

    // returns a 201 Created with a location header
    @POST
    public Response createRoom(Room room) {
        if (room.name == null || room.name.trim().isEmpty()) {
            return Response.status(400)
                    .entity(new ErrorBody(400, "Room name is required."))
                    .build();
        }
        room.id = UUID.randomUUID().toString();
        dataStore.rooms.put(room.id, room);

        URI location = URI.create("http://localhost:8080/rooms/" + room.id);
        return Response.created(location).entity(room).build();  // 201 and the location header
    }

    //
    @GET
    @Path("/{id}")
    public Response getRoom(@PathParam("id") String id) {
        Room room = dataStore.rooms.get(id);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorBody(404, "Room not found: " + id))
                    .build();
        }
        return Response.ok(room).build();
    }

    // blocked with 409 if sensors are assigned to it
    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        Room room = dataStore.rooms.get(id);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorBody(404, "Room not found: " + id))
                    .build();
        }

        boolean hasSensors = false;
        for (Sensor s : dataStore.sensors.values()) {
            if (id.equals(s.roomId)) { hasSensors = true; break; }
        }
        if (hasSensors) {
            throw new RoomNotEmptyException(id);  // 409
        }

        dataStore.rooms.remove(id);
        return Response.noContent().build();  // 204
    }
}
