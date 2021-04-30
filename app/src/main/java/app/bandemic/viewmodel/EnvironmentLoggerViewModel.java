package app.bandemic.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import app.bandemic.strict.database.Beacon;
import app.bandemic.strict.database.OwnUUID;
import app.bandemic.strict.repository.BroadcastRepository;

public class EnvironmentLoggerViewModel extends AndroidViewModel {

    private final BroadcastRepository mBroadcastRepository;

    private final LiveData<List<Beacon>> mDistinctBeacons;

    public EnvironmentLoggerViewModel(Application application) {
        super(application);
        //TODO: are two instances of repository ok (in ViewModel and TracingService)?
        mBroadcastRepository = new BroadcastRepository(application);
        mDistinctBeacons = mBroadcastRepository.getDistinctBeacons();
    }

    //todo do I need a refresh function as for uuids? this should update automatically

    public LiveData<List<Beacon>> getDistinctBeacons() {
        return mDistinctBeacons;
    }

    public LiveData<List<OwnUUID>> getAllOwnUUIDs() {
        return mBroadcastRepository.getAllOwnUUIDs();
    }
}

