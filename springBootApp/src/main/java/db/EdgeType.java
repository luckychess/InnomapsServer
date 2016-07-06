package db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alnedorezov on 6/16/16.
 */
@DatabaseTable(tableName = "Edge_types")
public class EdgeType {
    @DatabaseField(generatedId = true, unique = true)
    private int id;
    @DatabaseField(unique = true)
    private String name;
    @DatabaseField
    private Date modified = null;

    public EdgeType(int id, String name, String modifiedStr) throws ParseException {
        this.id = id;
        this.name = name;
        this.modified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(modifiedStr);
    }

    public EdgeType(int id, String name, Date modified) {
        this.id = id;
        this.name = name;
        this.modified = modified;
    }

    // For deserialization with Jackson
    public EdgeType() {
        // all persisted classes must define a no-arg constructor with at least package visibility
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getModified() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(modified);
    }
}