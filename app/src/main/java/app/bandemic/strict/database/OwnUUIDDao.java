package app.bandemic.strict.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OwnUUIDDao {
    @Query("SELECT * FROM ownuuid")
    LiveData<List<OwnUUID>> getAll();

    @Insert
    void insertAll(OwnUUID... uuids);


    @Delete
    void delete(OwnUUID ownUUID);
}
