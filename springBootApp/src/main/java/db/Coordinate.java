package db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alnedorezov on 6/9/16.
 */
@DatabaseTable(tableName = "Coordinate")
public class Coordinate {
    @DatabaseField(generatedId = true, unique = true)
    private int id;
    @DatabaseField(uniqueCombo = true)
    private double latitude;
    @DatabaseField(uniqueCombo = true)
    private double longitude;
    @DatabaseField(uniqueCombo = true)
    private int floor;
    @DatabaseField
    private String type;
    @DatabaseField
    private String name;
    @DatabaseField
    private String description;
    @DatabaseField
    private Integer roomNumber;

    public Coordinate(int id, double latitude, double longitude, int floor, String type, String name, String description, Integer roomNumber) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.floor = floor;
        this.type = type;
        this.name = name;
        this.description = description;
        this.roomNumber = roomNumber;
    }

    public Coordinate() {
        // all persisted classes must define a no-arg constructor with at least package visibility
    }

    public int getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getFloor() {
        return floor;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }
}
