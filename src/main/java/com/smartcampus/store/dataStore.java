package com.smartcampus.store;

// LOCATION: src/main/java/com/smartcampus/store/dataStore.java

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dataStore {
    public static Map<String, Room>                rooms    = new HashMap<String, Room>();
    public static Map<String, Sensor>              sensors  = new HashMap<String, Sensor>();
    public static Map<String, List<SensorReading>> readings = new HashMap<String, List<SensorReading>>();
}