package app.bandemic.strict.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

import app.bandemic.strict.database.AppDatabase;
import app.bandemic.strict.database.InfectedUUID;
import app.bandemic.strict.database.InfectedUUIDDao;
import app.bandemic.strict.database.Infection;
import app.bandemic.strict.network.InfectedUUIDResponse;
import app.bandemic.strict.network.InfectionIdsWebservice;
import app.bandemic.strict.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfectedUUIDRepository {

    private static final String LOG_TAG = "InfectedUUIDRepository";

    private final InfectionIdsWebservice webservice;

    private final InfectedUUIDDao infectedUUIDDao;

    public InfectedUUIDRepository(Application application) {
        webservice = RetrofitClient.getInfectionchainWebservice();
        AppDatabase db = AppDatabase.getDatabase(application);
        infectedUUIDDao = db.infectedUUIDDao();
    }
// this is needed locally by a recyclerview displaying locally
    public LiveData<List<InfectedUUID>> getInfectedUUIDs() {
        refreshInfectedUUIDs();
        return infectedUUIDDao.getAll();
    }

    public LiveData<List<Infection>> getPossiblyInfectedEncounters() {
        return infectedUUIDDao.getPossiblyInfectedEncounters();
    }
// method to get ids from api continuously..............................................
    public void refreshInfectedUUIDs() {
        webservice.getInfectedUUIDResponse().enqueue(new Callback<InfectedUUIDResponse>() {
            @Override
            public void onResponse(Call<InfectedUUIDResponse> call, Response<InfectedUUIDResponse> response) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    if(response.body() != null) {
                        infectedUUIDDao.insertAll(response.body().data.toArray(new InfectedUUID[response.body().data.size()]));
                    }
                    else {
                        // TODO: error handling!
                        Log.e(LOG_TAG, "Invalid response from api");
                    }

                });
            }

            @Override
            public void onFailure(Call<InfectedUUIDResponse> call, Throwable t) {
                // TODO error handling
                //Log.e(LOG_TAG, t.getCause().getMessage());
                //Log.e(LOG_TAG, t.getMessage() + t.getStackTrace().toString());
            }
        });
    }
}
