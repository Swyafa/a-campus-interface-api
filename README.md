# A Smart Campus  Sensor Api

## Overview

What is this?
This is a RESTful Java API developed to simulate a smart campus environment by managing rooms, sensors, and give live sensors readings using already in-memory data store.

The application was implemented using Jakarta RESTful web services (JAX-RS), Jersey, Grizzly, and Maven, In accordance with the coursework specification. Does not require SQL or NoSQL for database.

## Prerequisites 

Please install the following software before running the project:

| Software      |       Version       |   Confirmation |
|:--------------|:-------------------:|--------------:|
| Java JDK      |         26          | java --version |
| Apache Maven  |    3.6 or higher    |   mvn -version |
| Inteliji IDEA | Any recent version  |       optional |
| Postman       | Any recent version  |               |


To install Maven, download it from https://maven.apache.org and add it to your 
system PATH. On Windows, set Maven_HOME as an environment variable pointing to the Maven folder. 

## Building and Running the Project

### 1. Clone the repository

```bash
git clone https://github.com/Swyafa/a-campus-interface-api.git
cd SmartCampus-api
```
### 2. Install dependencies 

maven will automatically download all required dependencies (Jersey, Grizzly, Jackson)
defined in `pom.xml`:

```bash
mvn clean install
```
### 3. Run the server

```bash 
mvn exec:java -Dexec.mainClass="com.smartcampus.Main"
```
Or run Main.java directly from Intelli IDEA by right-clicking the file and selecting **Run "Main.main()**.

### 4. Confirm the server is running

An output is sent which you can see it being:
```
Smart Campus API running at http://localhost:8080/ Press ENTER to stop.
```
## Project Structure

```
src/main/java/com/smartcampus/
├── Main.java                               — Server entry point (Grizzly)
├── SmartCampusApplication.java             — @ApplicationPath("/api/v1")
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── store/
│   └── dataStore.java                      — In-memory ConcurrentHashMap store
├── resource/
│   ├── rootResource.java                   — GET /api/v1 discovery endpoint
│   ├── RoomResource.java                   — /rooms endpoints
│   ├── SensorResource.java                 — /sensors endpoints
│   └── SensorReadingResource.java          — /sensors/{id}/readings sub-resource
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


## API Design 

API follows REST principles.
All endpoints consume and produce application/json. All of the data is stored entirely in memory of the ConcurrentHashMap.


## Base URL

http://localhost:8080/api/v1/

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/` | Discovery endpoint — returns API metadata |
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{id}` | Get a single room by ID |
| DELETE | `/api/v1/rooms/{id}` | Delete a room (blocked if sensors assigned) |
| GET | `/api/v1/sensors` | List all sensors |
| GET | `/api/v1/sensors?type=CO2` | Filter sensors by type |
| POST | `/api/v1/sensors` | Create a new sensor |
| GET | `/api/v1/sensors/{id}` | Get a single sensor by ID |
| GET | `/api/v1/sensors/{id}/readings` | Get all readings for a sensor |
| POST | `/api/v1/sensors/{id}/readings` | Add a new reading to a sensor |



## Sample curl Commands 

### 1. Get API discovery metadata

```bash
curl -X GET http://localhost:8080/api/v1/
```

### 2. Create a room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Lab A", "capacity": 30}'
```

### 3. Create a sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "CO2", "status": "ACTIVE", "roomId": "ROOM-ID"}'
```

### 4. Filter sensors by type

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 5. Add a sensor reading

```bash
curl -X POST http://localhost:8080/api/v1/sensors/SENSOR-ID/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 412.5, "unit": "ppm"}'
```

### 6. Attempt to delete a room that has sensors (409)

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/ROOM-ID
```

### 7. Create a sensor with a non-existent roomId (422)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "TEMPERATURE", "roomId": "fake-id"}'
```

## Error Handling

Custom exceptions and ExceptionMapper classes are used to return structured JSON
responses. 


# Theory Questions
# Part 1.1 - JAX-RS 
Lifecycle and Shared In-Memory Data
JAX-RS creates a new resource instance for each incoming request. This is known as request-scoped
lifecycle behaviour. This means instance fields on a resource class are re-initialised with each request and cannot hold shared state between calls. Resource instances are recreated for each request, but shared static data remain available. 

As all resource instances share access to the static fields in 'dataStore', multiple simultanoues requsts can be read and written to at the same time which creates race conditions. e.g. two concurrent POST requests can both check a key doesn't exist, both can insert, causing one to overwrite the other.

to prevent this, 'dataStore' uses ConcurrentHashMap instead of 'HashMap' as it is a thread-safe by design and uses internal segment locking so any concurrent reads and writes don't corrupt data structure or cause a 'ConcurrentModificationException'. This is the correct synchronisation strategy for stateless request-scoped architecture that has shared in-memory state.
### Part 1.2 - Why is HATEOAS considered a hallmark of advanced RESTful design? How does it benefit client developers?

Hypermedia As The Engine Of Application State (HATEOAS) means that API responses include links to related or next available actions, instead of just using raw data. E.g, a room response might include a link to the sensors, and a sensor response might include a link to post a new reading.

This is beneficial for the clients in several ways. First, clients do not need to hardcode API URLs. They discover them dynamically from responses, this means that the client will be more resilient,tn to server-side URL changes. API has become a self-documenting to some extent for example a developer can explore the api by going through the links instead of having the need to use external documentation for each action. Clients having to send invalid requests is reduced due to the server being able to guide the client through correct state transitions. 
## Part 2.1 - ID vs Full Object 

What are implications of returning only IDs versus returning full room objects in a list response?
After making a resource with a POST request, an API can return either a new resource ID or the entire full created object. Returning the full object is more useful because the client immediately recieves all stores values without making the extra GET request.
This is good as it improves the efficiency by reducing any extra network calls and latency, this is really helpful for when the server needs to generate fields e.g. such as UUIDs or default values that the client requires.
Returning only the ID creates a smaller response payload which amy be preferable for larger resoruces. This project, returns the full object is suitable as the room and sensor are small.
## Part 2.2 - Idempotency of DELETE
A HTTP method is idempotent if it repeats the same request results in the same final server state. DELETE would be considered as idempotent as deleting a resource once and multiple times leaves the resources removed.
E.g. first request for 'DELETE /rooms/{id}' request removes the room, If the same request is sent again, room is not present/ has been removed, therefore the server state would not change.
Second request that can return is the '404 Not Found', with the first one returning '204 No Content'. This does not break idempotency as idempotency relates to the final state of the server and is not an identical response codes. 

## Part 3.1 - @Consumes and Content-Type Mismatches
The @Consumes(MediaType.APPLICATION_JSON) tells JAX-RS that a resource method is executed. The framework normally returns back a '415 Unsupported Media Type'.

This behaviour is useful as there is a clear contract between the client and server, ensuring only some data formats reach the application logic.

## Part 3.2 - @QueryParam vs @PathParam for filtering
@PathParam identifies a specific resource. e.g. '/rooms/{id}' which refers to a unique room, so the room id belongs inside of the path.

@QueryParam is suitable for optional filters or sorting options. As seen in this project, 'GET /sensors?type=CO2' filters the sensor
collection without identify a single sensor by the type. 

Using query parameters allows the api to be more flexible. If there is not filter, the endpoint would return all the sensors. However, having these extra paramneters allow for a much more accurate results without creating separate routes. 

## Part 4.1 - Sub Resource Locator Pattern

A sub-resource locator is a method that returns another resource class to handle nested routes. All the related child resources are delegated to separate classes, instead of placing all logic in one large resource class.

In this project, '/sensors/{id}/readings' is handled by 'SensorReadingResource', while 'SensorResource' remains focused on sensor-level operations. This ensures that all the responsibilities are separated and improves readability.

The pattern is useful in larger APIs as it prevents resource classes are too large and makes testing smaller components easier.

## Part 5.2 - Why 422 vs 404
When a client sends POST /api/v1/sensors with a roomId that doesn't exist, it returns a 404 Not found which would be misleading. 404 error means that the requested URL can not be found and not talking about the data that is clearly missing in this example as the requested URL was found correctly.

A 422 Unprocessable entity is more accurate in describing this error as it signals the server that the content type was correct and the JSON is valid, however, the instruction wasn't carried out due to a logical problem with the data. The request was correct but semantically incorrect. Returning a 422 shows the client that the URL was fine and that the JSON was parsed correct but a resource wasn't found rather assuming the endpoint was wrong. 


## Part 5.4 - Security Risk of Exposing Stack Traces
When returning raw Java stack traces to API clients, it poses a security risk as it reveals internal implementation details. This includes class names, package structures, file paths, and frameworks being used.

Any attackers can use this information to identify known vulnerabilities in specific libraries. This can be seen as unprofessional and may confuse normal users 

For that reason, this API uses a generic exception mapper which returns a '500 Internal Server Error' response while the detailed error is logged internall for debugging.

## Part 5.5 - JAX-RS Filters Vs Manual Logging
Adding logging statements manually inside each resource method can lead to duplicated code and inconsistency logging behaviour, also developers could also forget to add logging for every endpoint when new ones are created.

JAX-RS filters provides a much cleaner solution. A ContainerRequestFilter logs incoming requests, while 'ContainerResponseFilter' can log response status codes for every endpoint without the need for the developer to do it.

This centralises logging in one place which overall improves the maintainability and allows business logic inside resource classes that focuses on handling requests 


