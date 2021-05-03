package app.bandemic.strict.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import app.bandemic.strict.database.AppDatabase;
import app.bandemic.strict.database.Beacon;
import app.bandemic.strict.database.BeaconDao;
import app.bandemic.strict.database.OwnUUID;
import app.bandemic.strict.database.OwnUUIDDao;
// this class is inserting my own ids into my own id table and insert neighbour details into beacon table ,as well as collecting all distinct ids of beacons
public class BroadcastRepository {

    private final OwnUUIDDao mOwnUUIDDao;
    private final BeaconDao mBeaconDao;
    private final LiveData<List<OwnUUID>> mAllOwnUUIDs;
    private final LiveData<List<Beacon>> mAllBeacons;
    private final LiveData<List<Beacon>> mDistinctBeacons;
   // private Application application;



    public BroadcastRepository(Application application) {

        AppDatabase db = AppDatabase.getDatabase(application);
        mOwnUUIDDao = db.ownUUIDDao();
        mBeaconDao = db.beaconDao();
        mAllOwnUUIDs = mOwnUUIDDao.getAll();
        mAllBeacons = mBeaconDao.getAll();
        mDistinctBeacons = mBeaconDao.getAllDistinctBroadcast(); // this is not different to mBeaconDao.getAll(); LOOK FOR QUERY WHICH GETS DISTICT
    }

    public LiveData<List<OwnUUID>> getAllOwnUUIDs() {
        return mAllOwnUUIDs;
    } // since its live , it notifies the observer of all generated personal ids

    public LiveData<List<Beacon>> getAllBeacons() {
        return mAllBeacons;
    } // Notifies observer of all the beacons

    public LiveData<List<Beacon>> getDistinctBeacons() { // notifies observer of the  distinct beacons in proximity within the lifecycle active period
        return mDistinctBeacons;
    }

    public void insertOwnUUID(OwnUUID ownUUID) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mOwnUUIDDao.insertAll(ownUUID);
        });
    }

    public void insertBeacon(Beacon beacon) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mBeaconDao.insertAll(beacon);
        });
    }
}
