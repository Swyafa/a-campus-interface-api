package com.smartcampus.store;

// Student ID: w2069246
// Student Name: Mohammed Sami Bari

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory data store using ConcurrentHashMap.
 *
 * ConcurrentHashMap is used instead of HashMap because JAX-RS creates a new
 * resource instance per request by default (request-scoped lifecycle).
 * Multiple requests can therefore access this shared static state concurrently,
 * making thread-safe collections essential to prevent race conditions and
 * data corruption.
 */


public class dataStore {
    public static Map<String, Room>                rooms    = new ConcurrentHashMap<String, Room>();
    public static Map<String, Sensor>              sensors  = new ConcurrentHashMap<String, Sensor>();
    public static Map<String, List<SensorReading>> readings = new ConcurrentHashMap<String, List<SensorReading>>();
}
