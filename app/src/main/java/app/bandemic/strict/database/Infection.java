package app.bandemic.strict.database;

import androidx.room.ColumnInfo;

import java.util.Date;

public class Infection {
    @ColumnInfo(name = "id")//1
    public int infectionId;
    @ColumnInfo(name = "timestamp")//2
    public Date encounterDate;
    public double distance;//3
    public Date createdOn;//4
    public int distrustLevel;//5
    public String icdCode;//6



}
