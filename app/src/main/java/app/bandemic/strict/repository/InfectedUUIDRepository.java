package app.bandemic.strict.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import app.bandemic.strict.database.AppDatabase;
import app.bandemic.strict.database.InfectedUUID;
import app.bandemic.strict.database.InfectedUUIDDao;
import app.bandemic.strict.database.Infection;
import app.bandemic.strict.network.InfectedUUIDResponse;
//import app.bandemic.strict.network.InfectionIdsWebservice;
import app.bandemic.strict.network.OwnUUIDResponse;
//import app.bandemic.strict.network.RetrofitClient;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;

import static android.content.ContentValues.TAG;

public class InfectedUUIDRepository {

    private static final String LOG_TAG = "InfectedUUIDRepository";

    //private final InfectionIdsWebservice webservice;

    private final InfectedUUIDDao infectedUUIDDao;
    private final DatabaseReference mPostReference= FirebaseDatabase.getInstance().getReference().child("IDs_From_Patients");

    public InfectedUUIDRepository(Application application) {
       // webservice = RetrofitClient.getInfectionchainWebservice();
        AppDatabase db = AppDatabase.getDatabase(application);
        infectedUUIDDao = db.infectedUUIDDao();
    }
// this is needed locally by a recyclerview displaying locally....THIS METHOD INSERTS INFECTED INTO LOCAL AND RETRIEVES AS WELL THAT LIST
    public LiveData<List<InfectedUUID>> getInfectedUUIDs() {
        refreshInfectedUUIDs();
        return infectedUUIDDao.getAll();
    }

    public LiveData<List<Infection>> getPossiblyInfectedEncounters() {
        return infectedUUIDDao.getPossiblyInfectedEncounters();
    }
// method to get ids from api continuously..............................................WE NEED TO MODIFY HERE AND RETRIEVE FROM THE FIREBASE DB
//    public void refreshInfectedUUIDs() {
//        webservice.getInfectedUUIDResponse().enqueue(new Callback<InfectedUUIDResponse>() {
//            @Override
//            public void onResponse(Call<InfectedUUIDResponse> call, Response<InfectedUUIDResponse> response) {
//                AppDatabase.databaseWriteExecutor.execute(() -> {
//                    if(response.body() != null) {
//                        infectedUUIDDao.insertAll(response.body().data.toArray(new InfectedUUID[response.body().data.size()]));
//                    }
//                    else {
//                        // TODO: error handling!
//                        Log.e(LOG_TAG, "Invalid response from api");
//                    }
//
//                });
//            }
//
//            @Override
//            public void onFailure(Call<InfectedUUIDResponse> call, Throwable t) {
//                // TODO error handling
//                //Log.e(LOG_TAG, t.getCause().getMessage());
//                //Log.e(LOG_TAG, t.getMessage() + t.getStackTrace().toString());
//            }
//        });
//    }
    public void refreshInfectedUUIDs() {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                InfectedUUIDResponse post = dataSnapshot.getValue(InfectedUUIDResponse.class);

                AppDatabase.databaseWriteExecutor.execute(() -> {
                    if(post != null) {
                        infectedUUIDDao.insertAll(post.data.toArray(new InfectedUUID[post.data.size()]));
                    }
                    else {
                        // TODO: error handling!
                        Log.e(LOG_TAG, "Invalid response from api");
                    }

                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mPostReference.addValueEventListener(postListener);
    }
}
