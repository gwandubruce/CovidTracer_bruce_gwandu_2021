package app.bandemic.strict.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
// LiveData is a data holder that can be observed within a given data cycle
@Dao
public interface BeaconDao {
    @Query("SELECT * FROM beacon")
    LiveData<List<Beacon>> getAll();

    @Query("SELECT  * FROM beacon")    // added DISTINCT receivedDoubleHash
    LiveData<List<Beacon>> getAllDistinctBroadcast();  // look for query to get distinct beacons because it is the same with getAll()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Beacon... beacons);

    @Delete
    void delete(Beacon beacon);
}
