package app.bandemic.strict.repository;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
//import com.google.gson.Gson;

import java.io.SyncFailedException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private ArrayList<ShellClass> shellAllClasses = new ArrayList<>();

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
               // List<Object> list = new ArrayList<>();
                InfectedUUID infectedUUID;
                ArrayList<ShellClass> shellClasses = new ArrayList<>();
                List<InfectedUUID> listInfectedUUID = new ArrayList<>();

               // snapshot.getChildrenCount();

               // GenericTypeIndicator<ArrayList<Object>> t = new GenericTypeIndicator<ArrayList<Object>>() {};

                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {

                    for (DataSnapshot x : childDataSnapshot.getChildren()){
                        String most = Objects.requireNonNull(x.child("ownUUID").child("mostSignificantBits").getValue()).toString();
                        String least = Objects.requireNonNull(x.child("ownUUID").child("leastSignificantBits").getValue()).toString();
                        String timestampDate = Objects.requireNonNull(x.child("timestamp").child("date").getValue()).toString();
                        String timestampDay  = Objects.requireNonNull(x.child("timestamp").child("day").getValue()).toString();
                        shellClasses.add(new ShellClass(most,least,timestampDate,timestampDay));
                    };
                }


                for (ShellClass c:shellClasses){
                    long least= Long.parseLong(c.getLeast());
                    long most= Long.parseLong(c.getMost());
                    Date date = new Date();

                    // Long most= ((Long) ((HashMap) ((Map) obj).get("ownUUID")).get("mostSignificantBits"));
                    ByteBuffer inputBuffer = ByteBuffer.wrap(new byte[/*Long.BYTES*/ 8 * 2]);
                    inputBuffer.putLong(0, least);
                    inputBuffer.putLong(4, most);

                    byte[] broadcastData;

                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        broadcastData = digest.digest(inputBuffer.array());
                        broadcastData = Arrays.copyOf(broadcastData, 26);  // removed 27 ndikaisa 26 as it is the hash length
                      //  broadcastData[26] = getTransmitPower();
                    } catch (NoSuchAlgorithmException e) {
                        Log.wtf(LOG_TAG, "Algorithm not found", e);
                        throw new RuntimeException(e);
                    }

                    infectedUUID = new InfectedUUID();
                    infectedUUID.hashedId=broadcastData;
                    infectedUUID.createdOn=date;
                    infectedUUID.distrustLevel=8;
                    infectedUUID.icdCode="Positive";
                    listInfectedUUID.add(infectedUUID);

                }

//                System.out.println("--------xxxxxxxxxxxxxxxxWWWWWWWWWWWWWWWWWWWWWWxxxXXXXXXXXXXXXXXXXXXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx----------------------"+list);

                AppDatabase.databaseWriteExecutor.execute(() -> {

                    if(shellClasses != null) {
                        //use shellClass getters

                        infectedUUIDDao.insertAll(listInfectedUUID.toArray(new InfectedUUID[0]));
                    }
                    else {
                        // TODO: error handling!
                        Log.e(LOG_TAG, "Invalid response from api");
                    }

                });

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                InfectedUUID infectedUUID;
                ArrayList<ShellClass> shellClasses = new ArrayList<>();
                List<InfectedUUID> listInfectedUUID = new ArrayList<>();

                // snapshot.getChildrenCount();

                // GenericTypeIndicator<ArrayList<Object>> t = new GenericTypeIndicator<ArrayList<Object>>() {};

                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {

                    for (DataSnapshot x : childDataSnapshot.getChildren()){
                        String most = Objects.requireNonNull(x.child("ownUUID").child("mostSignificantBits").getValue()).toString();
                        String least = Objects.requireNonNull(x.child("ownUUID").child("leastSignificantBits").getValue()).toString();
                        String timestampDate = Objects.requireNonNull(x.child("timestamp").child("date").getValue()).toString();
                        String timestampDay  = Objects.requireNonNull(x.child("timestamp").child("day").getValue()).toString();
                        shellClasses.add(new ShellClass(most,least,timestampDate,timestampDay));
                    };
                }


                for (ShellClass c:shellClasses){
                    long least= Long.parseLong(c.getLeast());
                    long most= Long.parseLong(c.getMost());
                    Date date = new Date();

                    // Long most= ((Long) ((HashMap) ((Map) obj).get("ownUUID")).get("mostSignificantBits"));
                    ByteBuffer inputBuffer = ByteBuffer.wrap(new byte[/*Long.BYTES*/ 8 * 2]);
                    inputBuffer.putLong(0, least);
                    inputBuffer.putLong(4, most);

                    byte[] broadcastData;

                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        broadcastData = digest.digest(inputBuffer.array());
                        broadcastData = Arrays.copyOf(broadcastData, 27);
                        broadcastData[26] = getTransmitPower();
                    } catch (NoSuchAlgorithmException e) {
                        Log.wtf(LOG_TAG, "Algorithm not found", e);
                        throw new RuntimeException(e);
                    }

                    infectedUUID = new InfectedUUID();
                    infectedUUID.hashedId=broadcastData;
                    infectedUUID.createdOn=date;
                    infectedUUID.distrustLevel=8;
                    infectedUUID.icdCode="Positive";
                    listInfectedUUID.add(infectedUUID);

                }

//                System.out.println("--------xxxxxxxxxxxxxxxxWWWWWWWWWWWWWWWWWWWWWWxxxXXXXXXXXXXXXXXXXXXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx----------------------"+list);

                AppDatabase.databaseWriteExecutor.execute(() -> {

                    //use shellClass getters

                    infectedUUIDDao.insertAll(listInfectedUUID.toArray(new InfectedUUID[0]));

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

class ShellClass{
    private String most;
    private String least;

    public String getMost() {
        return most;
    }

    public void setMost(String most) {
        this.most = most;
    }

    public String getLeast() {
        return least;
    }

    public void setLeast(String least) {
        this.least = least;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    private String date;
    private String day;

    ShellClass(String most,String least,String date,String day){
        this.most = most;
        this.least = least;
        this.date = date;
        this.day = day;
    }

}
