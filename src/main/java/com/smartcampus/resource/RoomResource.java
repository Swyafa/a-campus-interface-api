package com.smartcampus.resource;

// LOCATION: src/main/java/com/smartcampus/resource/RoomResource.java

import com.smartcampus.exception.ErrorBody;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.dataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // GET /rooms
    @GET
    public List<Room> getRooms() {
        return new ArrayList<>(dataStore.rooms.values());
    }

    // POST /rooms — create room, returns 201
    @POST
    public Response createRoom(Room room) {
        if (room.name == null || room.name.trim().isEmpty()) {
            return Response.status(400)
                    .entity(new ErrorBody(400, "Room name is required."))
                    .build();
        }
        room.id = UUID.randomUUID().toString();
        dataStore.rooms.put(room.id, room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // GET /rooms/{id}
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

    // DELETE /rooms/{id} — blocked with 409 if sensors still assigned
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
        for (com.smartcampus.model.Sensor s : dataStore.sensors.values()) {
            if (id.equals(s.roomId)) { hasSensors = true; break; }
        }
        if (hasSensors) {
            throw new RoomNotEmptyException(id);
        }
        dataStore.rooms.remove(id);
        return Response.noContent().build();  // 204
    }
}