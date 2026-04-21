package com.smartcampus;

// LOCATION: src/main/java/com/smartcampus/Main.java

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Main {
    public static void main(String[] args) throws IOException {

        ResourceConfig config = new ResourceConfig();
        config.packages(
                "com.smartcampus.resource",
                "com.smartcampus.exception",
                "com.smartcampus.filter"
        );
        config.register(JacksonFeature.class);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create("http://localhost:4206/"),
                config
        );

        System.out.println("Smart Campus API running at http://localhost:4206/");
        System.out.println("Press ENTER to stop.");
        System.in.read();
        server.shutdown();
    }
}