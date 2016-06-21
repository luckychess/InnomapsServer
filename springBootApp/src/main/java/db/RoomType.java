package db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by alnedorezov on 6/17/16.
 */
@DatabaseTable(tableName = "Room_types")
public class RoomType {
    @DatabaseField(generatedId = true, unique = true)
    private int id;
    @DatabaseField(unique = true)
    private String name;

    public RoomType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // For deserialization with Jackson
    public RoomType() {
        // all persisted classes must define a no-arg constructor with at least package visibility
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}