# Smart Campus Sensor API

---

## Prerequisites

Before running this project, ensure the following are installed on your machine:

| Tool | Version | Verify |
|------|---------|--------|
| Java JDK | 26 (build 26+35-2893) | `java -version` |
| Apache Maven | 3.6 or higher | `mvn -version` |
| IntelliJ IDEA | Any recent version | — |
| Postman | Any recent version | For API testing |

To install Maven, download it from https://maven.apache.org/download.cgi and add it to your system PATH. On Windows, set `MAVEN_HOME` as an environment variable pointing to the Maven folder, then add `%MAVEN_HOME%\bin` to PATH.

---

## Building and Running the Project

### 1. Clone the repository

```bash
git clone https://github.com/[YOUR-USERNAME]/SmartCampus-api.git
cd SmartCampus-api
```

### 2. Install dependencies

Maven will automatically download all required dependencies (Jersey, Grizzly, Jackson) defined in `pom.xml`:

```bash
mvn clean install
```

### 3. Run the server

```bash
mvn exec:java -Dexec.mainClass="com.smartcampus.Main"
```

Or run `Main.java` directly from IntelliJ IDEA by right-clicking the file and selecting **Run 'Main.main()'**.

### 4. Confirm the server is running

You should see the following output in the console:

```
Smart Campus API running at http://localhost:8080/
Press ENTER to stop.
```

The API is now accessible at `http://localhost:8080/`.

---

## Project Structure

```
src/main/java/com/smartcampus/
├── Main.java                          — Server entry point (Grizzly)
├── SmartCampusApplication.java        — @ApplicationPath("/api/v1")
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── store/
│   └── dataStore.java                 — In-memory ConcurrentHashMap store
├── resource/
│   ├── rootResource.java              — GET /api/v1 discovery endpoint
│   ├── RoomResource.java              — /rooms endpoints
│   ├── SensorResource.java            — /sensors endpoints
│   └── SensorReadingResource.java     — /sensors/{id}/readings (sub-resource)
├── exception/
│   ├── ErrorBody.java
│   ├── RoomNotEmptyException.java
│   ├── LinkedResourceNotFoundException.java
│   ├── SensorUnavailableException.java
│   ├── RoomNotEmptyExceptionMapper.java
│   ├── LinkedResourceNotFoundExceptionMapper.java
│   ├── SensorUnavailableExceptionMapper.java
│   └── GenericExceptionMapper.java
└── filter/
    └── LoggingFilter.java
```

---

## API Design

The API follows REST principles. All endpoints consume and produce `application/json`. Data is stored entirely in memory using `ConcurrentHashMap` — no database is used.

### Base URL

```
http://localhost:8080
```

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1` | Discovery endpoint — returns API metadata |
| GET | `/rooms` | List all rooms |
| POST | `/rooms` | Create a new room |
| GET | `/rooms/{id}` | Get a single room by ID |
| DELETE | `/rooms/{id}` | Delete a room (blocked if sensors assigned) |
| GET | `/sensors` | List all sensors |
| GET | `/sensors?type=CO2` | Filter sensors by type |
| POST | `/sensors` | Create a new sensor |
| GET | `/sensors/{id}` | Get a single sensor by ID |
| GET | `/sensors/{id}/readings` | Get all readings for a sensor |
| POST | `/sensors/{id}/readings` | Add a new reading to a sensor |

---

## Sample curl Commands

### 1. Get API discovery metadata

```bash
curl -X GET http://localhost:8080/api/v1
```

Expected response:
```json
{
  "name": "Smart Campus Sensor API",
  "version": "v1",
  "contact": "admin@smartcampus.ac.uk",
  "resources": {
    "rooms": "http://localhost:8080/rooms",
    "sensors": "http://localhost:8080/sensors"
  }
}
```

### 2. Create a room

```bash
curl -X POST http://localhost:8080/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Lab A", "capacity": 30}'
```

Expected: `201 Created` with a `Location` header and the created room object including its generated UUID.

### 3. Create a sensor linked to a room

```bash
curl -X POST http://localhost:8080/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "CO2", "status": "ACTIVE", "roomId": "<room-id-here>"}'
```

Expected: `201 Created` with the sensor object.

### 4. Filter sensors by type

```bash
curl -X GET "http://localhost:8080/sensors?type=CO2"
```

Expected: `200 OK` with an array containing only CO2 sensors.

### 5. Add a sensor reading

```bash
curl -X POST http://localhost:8080/sensors/<sensor-id>/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 412.5, "unit": "ppm"}'
```

Expected: `201 Created` with the reading object. The parent sensor's `currentValue` is also updated to `412.5`.

### 6. Attempt to delete a room that has sensors (triggers 409)

```bash
curl -X DELETE http://localhost:8080/rooms/<room-id-with-sensors>
```

Expected: `409 Conflict`
```json
{"status": 409, "error": "Room <id> cannot be deleted because it still has sensors assigned to it."}
```

### 7. Create a sensor with a non-existent roomId (triggers 422)

```bash
curl -X POST http://localhost:8080/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "TEMPERATURE", "roomId": "fake-id"}'
```

Expected: `422 Unprocessable Entity`
```json
{"status": 422, "error": "Cannot create sensor: room 'fake-id' does not exist."}
```

### 8. Post a reading to a MAINTENANCE sensor (triggers 403)

```bash
curl -X POST http://localhost:8080/sensors/<maintenance-sensor-id>/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 55.0, "unit": "celsius"}'
```

Expected: `403 Forbidden`
```json
{"status": 403, "error": "Sensor <id> is currently in MAINTENANCE and cannot accept readings."}
```

---

## Theory Questions

### 1.1 — JAX-RS Lifecycle and In-Memory Data Synchronisation

By default, JAX-RS creates a new instance of each resource class for every incoming HTTP request. This is the **request-scoped lifecycle**. Each request gets its own `RoomResource` or `SensorResource` object, which is created, used, and then discarded. The alternative is **singleton lifecycle**, where one shared instance handles all requests, configured with `@Singleton`.

Because resource instances are request-scoped, they do not share state with each other — however, they do all share access to the static fields in `dataStore`, which is a class-level (static) store. When multiple requests arrive simultaneously, they can all read and write to `dataStore` at the same time, creating race conditions. For example, two concurrent `POST /rooms` requests could both check that a key does not exist, then both insert, causing one to silently overwrite the other.

To prevent this, `dataStore` uses `ConcurrentHashMap` instead of `HashMap`. `ConcurrentHashMap` is thread-safe by design — it uses internal segment locking so that concurrent reads and writes do not corrupt the data structure or cause `ConcurrentModificationException`. This is the correct synchronisation strategy for a stateless request-scoped architecture with shared in-memory state.

---

### 1.2 — HATEOAS and the Discovery Endpoint

HATEOAS (Hypermedia as the Engine of Application State) is a REST constraint that requires APIs to include links to related resources in their responses, making the API self-describing. Rather than requiring clients to read external documentation to know what URLs exist, the API itself tells them.

The discovery endpoint at `GET /api/v1` implements this by returning a JSON object that includes a `resources` map, listing the URLs for rooms and sensors. A client hitting the API for the first time can immediately discover what is available without consulting a separate specification document.

The key benefit over static documentation is resilience to change. If a URL changes, static docs become stale and break clients that relied on them. With HATEOAS, the client always follows links returned by the server, so URL changes propagate automatically. This also reduces coupling between the client and server, since the client does not need to hardcode paths.

---

### 2.1 — ID-Only vs Full-Object Returns

When a POST request creates a resource, the API can return either just the generated ID or the full object. Returning the full object — as this API does — is generally preferable because it saves the client from making a second GET request to retrieve the data it just created. This reduces network round-trips and latency, which is particularly important in mobile or high-traffic environments.

However, returning the full object does increase the size of the response payload. For simple resources like rooms this overhead is negligible. For very large resources with many fields or nested objects, returning only the ID (or a minimal representation) reduces bandwidth. The `Location` header returned with every `201 Created` response provides the URL of the new resource, giving clients the option to fetch the full object later if they choose not to use the body returned immediately.

---

### 2.2 — Idempotency of DELETE

An HTTP operation is idempotent if making the same request multiple times produces the same server state as making it once. DELETE is defined as idempotent by the HTTP specification. If you delete a room that exists, it is removed. If you then send the exact same DELETE request again, the room is already gone and the server state has not changed further — there is still no room with that ID.

However, idempotency refers to server state, not response codes. The first DELETE returns `204 No Content`. The second DELETE returns `404 Not Found` because the room no longer exists. The response code differs, but the underlying state of the server is identical after both calls — the room is absent. This is the correct behaviour. It would be incorrect to re-delete something that does not exist and return `204` again, as this would be misleading, though some APIs do implement it that way for strict idempotency of the response as well.

---

### 3.1 — @Consumes and Content-Type Mismatches

The `@Consumes(MediaType.APPLICATION_JSON)` annotation on a resource method tells JAX-RS that the method only accepts requests with a `Content-Type: application/json` header. If a client sends a request with a different content type, such as `text/plain` or `application/xml`, JAX-RS will automatically reject the request before the method is even called.

The HTTP status code returned in this case is **415 Unsupported Media Type**. This is handled entirely by the JAX-RS framework without requiring any custom exception code. The client must correct its `Content-Type` header and resend the request. This behaviour enforces a strict contract between the client and the API, preventing malformed or unexpected data formats from reaching the business logic.

---

### 3.2 — @QueryParam vs @PathParam for Filtering

`@PathParam` is used to identify a specific resource. For example, `/rooms/{id}` uses a path parameter because the ID uniquely identifies one room in the collection. Path parameters are part of the resource's identity — the URL `/rooms/123` refers to a fundamentally different resource than `/rooms/456`.

`@QueryParam` is used for optional, non-identifying parameters that modify how a collection is returned. Filtering sensors by type with `GET /sensors?type=CO2` is the correct design because the type is not identifying a specific resource — it is narrowing a collection based on a property. Using a path parameter for this, such as `GET /sensors/CO2`, would be semantically incorrect because it implies CO2 is an ID rather than a filter criterion.

Query parameters are also optional by nature. `GET /sensors` with no query parameter returns all sensors, while `GET /sensors?type=CO2` filters them. This behaviour is impossible to express cleanly with path parameters without defining separate routes.

---

### 4.1 — Sub-Resource Locator Pattern

The sub-resource locator pattern allows a resource method to delegate request handling to a separate class rather than handling it directly. In this API, `SensorResource` contains a method annotated only with `@Path("/{id}/readings")` and no HTTP verb annotation. When Jersey receives a request to `/sensors/{id}/readings`, it calls this method, which returns a new `SensorReadingResource` instance. Jersey then dispatches the actual GET or POST to that instance.

This pattern keeps each class focused on a single responsibility. `SensorResource` handles sensor-level concerns, and `SensorReadingResource` handles reading-level concerns. In a large API with dozens of resources and sub-resources, this separation prevents resource classes from growing unmanageable. It also makes the code easier to test in isolation, since each class can be unit tested independently. The locator itself can also perform validation — in this case, it receives the `sensorId` and passes it to the reading resource, so the reading resource always knows which sensor it belongs to.

---

### 5.2 — Why 422 is More Appropriate Than 404

When a client sends a `POST /sensors` request with a `roomId` that does not exist, returning `404 Not Found` would be misleading. HTTP 404 means the requested URL could not be found — but in this case, the URL `/sensors` was found correctly. The problem is that the data inside the request body references a room that does not exist.

HTTP 422 Unprocessable Entity is semantically correct here because it means the server understood the request, the content type was correct, the JSON was valid, but the instruction itself could not be carried out because of a logical problem with the data. The request was well-formed but semantically invalid. Returning 422 gives the client precise information: the URL is fine, the JSON parsed correctly, but a referenced resource was not found. This distinction helps clients debug the actual problem rather than assuming they called the wrong endpoint.

---

### 5.4 — Security Risks of Exposing Stack Traces

Returning a raw Java stack trace in an API error response is a significant security risk. Stack traces reveal internal implementation details that attackers can use to plan targeted attacks. Specifically, a stack trace exposes the names of internal Java classes and packages, which reveals the framework and libraries in use (e.g. Jersey, Grizzly, Jackson) along with their versions, allowing attackers to look up known CVEs for those exact versions. It also exposes file paths and line numbers of the server's source code, which helps an attacker understand the structure of the codebase and identify where logic errors might exist.

The `GenericExceptionMapper` in this API catches all `Throwable` instances and returns a generic `500 Internal Server Error` with only the message "An unexpected error occurred." The actual exception is never sent to the client. Internally, the exception can still be logged server-side for debugging, but the client receives no information that could aid an attacker. This follows the principle of least privilege — exposing only what is necessary.

---

### 5.5 — JAX-RS Filters vs Manual Logging

Placing `Logger.info()` calls directly inside each resource method is a fragile approach. If a new endpoint is added, the developer must remember to add logging manually. If the logging format needs to change, every method must be updated individually. This scatters a cross-cutting concern across the entire codebase.

JAX-RS filters solve this architecturally. A class implementing `ContainerRequestFilter` and `ContainerResponseFilter` is registered once and automatically intercepts every request and response without touching any resource class. This follows the separation of concerns principle — logging is handled in one place, independently of business logic. It also makes it impossible to accidentally omit logging from a new endpoint, since filters apply globally. The `@Provider` annotation ensures Jersey discovers and registers the filter automatically through package scanning, requiring no manual registration per endpoint.

---

## Design Decisions

- **No database** — all data is stored in static `ConcurrentHashMap` collections in `dataStore.java`, as required by the coursework specification
- **UUID generation** — room and sensor IDs are generated server-side using `UUID.randomUUID()`, ensuring uniqueness without requiring clients to supply IDs
- **Sub-resource locator** — `/sensors/{id}/readings` is handled by a dedicated `SensorReadingResource` class, keeping responsibilities separated
- **ErrorBody POJO** — all error responses use a consistent `{"status": N, "error": "..."}` structure via the `ErrorBody` class, rather than raw strings
- **ConcurrentHashMap** — used instead of `HashMap` to handle concurrent requests safely without explicit synchronisation blocks
