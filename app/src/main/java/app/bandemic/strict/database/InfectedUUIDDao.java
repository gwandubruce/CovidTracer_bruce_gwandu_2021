package app.bandemic.strict.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface InfectedUUIDDao {
    @Query("SELECT * FROM infecteduuid")
    LiveData<List<InfectedUUID>> getAll();


    // TODO: change != to =, this is just for demo!!!!
    @Query("SELECT infecteduuid.id, beacon.timestamp, distance, createdOn, distrustLevel, icdCode" +
            " FROM infecteduuid JOIN beacon ON" +
            " infecteduuid.hashedId != beacon.receivedDoubleHash")  // this is where we compare infected ids and locally received beacons id and pull all such data
    LiveData<List<Infection>> getPossiblyInfectedEncounters();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(InfectedUUID... infectedUUID);

    @Delete
    void delete(InfectedUUID infectedUUID);
}
