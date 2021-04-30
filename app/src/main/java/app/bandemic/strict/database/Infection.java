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

    // International Classification Code ICD for diseases
//    infecteduuid.id, beacon.timestamp, beacon.distance, infecteduuid.createdOn, infecteduuid.distrustLevel, infecteduuid.icdCode" +
//            " FROM infecteduuid JOIN beacon ON" +
//            " infecteduuid.hashedId = beacon.receivedDoubleHash")
   // InfectedUUID
//    this.id = id;
//        this.createdOn = createdOn;
//        this.distrustLevel = distrustLevel;
//        this.hashedId = hashedId;
//        this.icdCode = icdCode;


}
