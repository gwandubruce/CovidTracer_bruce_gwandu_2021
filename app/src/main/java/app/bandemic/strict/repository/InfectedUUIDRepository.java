package app.bandemic.strict.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.bandemic.strict.database.AppDatabase;
import app.bandemic.strict.database.InfectedUUID;
import app.bandemic.strict.database.InfectedUUIDDao;
import app.bandemic.strict.database.Infection;
import app.bandemic.strict.database.OwnUUID;
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
    private final Query mPostReference= FirebaseDatabase.getInstance().getReference().child("ListOfOwnUUID_Objects").orderByChild("data");
   // private final Query mPostReference= FirebaseDatabase.getInstance().getReference().child("ListOfOwnUUID_Objects");

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
        ChildEventListener postListener = new ChildEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // Get Post object and use the values to update the UI
////                OwnUUIDResponse post = dataSnapshot.getValue(OwnUUIDResponse.class);
////                System.err.println("------------------------------------------------------------------------------------"+post.getData());
//                List<OwnUUID> list = new ArrayList<>();
//                dataSnapshot.getChildrenCount();
//                //List<User> list= new ArrayList<User>();
//                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
//                    OwnUUIDResponse ownUUIDResponse = childDataSnapshot.getValue(OwnUUIDResponse.class);
//                    list.addAll(ownUUIDResponse.data);
//                }
//
//                System.out.println("--------xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx----------------------"+list);
//
//                AppDatabase.databaseWriteExecutor.execute(() -> {
//                    if(list != null) {
//                        infectedUUIDDao.insertAll(list.toArray(new OwnUUID[list.size()]));
//                    }
//                    else {
//                        // TODO: error handling!
//                        Log.e(LOG_TAG, "Invalid response from api");
//                    }
//
//                });
//            }

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                List<Object> list = new ArrayList<>();
                snapshot.getChildrenCount();
                //List<User> list= new ArrayList<User>();
                GenericTypeIndicator<ArrayList<Object>> t = new GenericTypeIndicator<ArrayList<Object>>() {};
              //  List<Message> messages = snapshot.getValue(t);
                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                   // OwnUUIDResponse ownUUID = snapshot.getValue(OwnUUIDResponse.class);
                   ArrayList<Object> ownUUID = childDataSnapshot.getValue(t);
                    list.addAll(ownUUID);
                }


                System.out.println("--------xxxxxxxxxxxxxxxxWWWWWWWWWWWWWWWWWWWWWWxxxXXXXXXXXXXXXXXXXXXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx----------------------"+list);

                AppDatabase.databaseWriteExecutor.execute(() -> {
                    if(list != null) {
                        infectedUUIDDao.insertAll(list.toArray(new InfectedUUID[list.size()]));
                    }
                    else {
                        // TODO: error handling!
                        Log.e(LOG_TAG, "Invalid response from api");
                    }

                });

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                List<Object> list = new ArrayList<>();
                snapshot.getChildrenCount();
                //List<User> list= new ArrayList<User>();
                GenericTypeIndicator<ArrayList<Object>> t = new GenericTypeIndicator<ArrayList<Object>>() {};
                //  List<Message> messages = snapshot.getValue(t);
                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {

                    ArrayList<Object> ownUUIDObject = childDataSnapshot.getValue(t);
                   // Map<String,Long> f=new HashMap<>();                          // for tests
                    for (Object obj:ownUUIDObject){
                        System.out.println(((HashMap) obj).get("ownUUID"));

                    }


//          ------------------------------------------------------------------------------------------------------------------
                    // Convert the UUID to its SHA-256 hash
//                    ByteBuffer inputBuffer = ByteBuffer.wrap(new byte[/*Long.BYTES*/ 8 * 2]);
//                    inputBuffer.putLong(0, currentUUID.getMostSignificantBits());
//                    inputBuffer.putLong(4, currentUUID.getLeastSignificantBits());
//
//                    byte[] broadcastData;
//
//                    try {
//                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
//                        broadcastData = digest.digest(inputBuffer.array());
//                        broadcastData = Arrays.copyOf(broadcastData, 27);
//                        broadcastData[26] = getTransmitPower();
//                    } catch (NoSuchAlgorithmException e) {
//                        Log.wtf(LOG_TAG, "Algorithm not found", e);
//                        throw new RuntimeException(e);
//                    }
                    //list.addAll(ownUUID);
                   // System.out.println("xxxxxxxxxxxxxxxxWWWWWWWWWWWWWWWWWWWWWWxxxXXXXXXXXXXXXXXXXXXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx--------------"+ownUUID);
                }


                //System.out.println("--------xxxxxxxxxxxxxxxxWWWWWWWWWWWWWWWWWWWWWWxxxXXXXXXXXXXXXXXXXXXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx----------------------"+list);

                AppDatabase.databaseWriteExecutor.execute(() -> {
                    if(list != null) {
                        infectedUUIDDao.insertAll(list.toArray(new InfectedUUID[list.size()]));
                    }
                    else {
                        // TODO: error handling!
                        Log.e(LOG_TAG, "Invalid response from api");
                    }

                });

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mPostReference.addChildEventListener(postListener);
    }
    private byte getTransmitPower() {
        // TODO look up transmit power for current device
        return (byte) -65;
    }
}
