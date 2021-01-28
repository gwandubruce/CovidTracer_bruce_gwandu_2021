package app.bandemic.strict.database;

import androidx.room.ColumnInfo;

import java.util.Date;

public class Infection {
    @ColumnInfo(name = "id")
    public int infectionId;
    @ColumnInfo(name = "timestamp")
    public Date encounterDate;
    public double distance;
    public Date createdOn;
    public int distrustLevel;
    public String icdCode;

    // International Classification Code ICD for diseases

    // In ICD-11, the code for the confirmed diagnosis of COVID-19 is RA01. 0 and the code for the clinical diagnosis (suspected or probable) of COVID-19 is RA01. 1.
}
