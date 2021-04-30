package app.bandemic.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import app.bandemic.strict.database.Infection;
import app.bandemic.strict.repository.InfectedUUIDRepository;

public class InfectionCheckViewModel extends AndroidViewModel {

    private final InfectedUUIDRepository mRepository;

    //private  LiveData<List<Infection>> possiblyInfectedEncounters;

    public InfectionCheckViewModel(Application application) {
        super(application);
        mRepository = new InfectedUUIDRepository(application);
       // possiblyInfectedEncounters = mRepository.getPossiblyInfectedEncounters();
    }

    public void refreshInfectedUUIDs() {
        mRepository.refreshInfectedUUIDs();
    }

    public LiveData<List<Infection>> getPossiblyInfectedEncounters() {

        return mRepository.getPossiblyInfectedEncounters(); }

}
