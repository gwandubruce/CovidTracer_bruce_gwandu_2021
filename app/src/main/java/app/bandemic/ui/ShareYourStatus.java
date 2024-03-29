package app.bandemic.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import app.bandemic.R;
import app.bandemic.strict.database.AppDatabase;
import app.bandemic.strict.database.OwnUUID;
import app.bandemic.strict.network.OwnUUIDResponse;
import app.bandemic.strict.repository.BroadcastRepository;
import app.bandemic.viewmodel.EnvironmentLoggerViewModel;

public class ShareYourStatus extends AppCompatActivity {
  private EditText enterPinTextBox;
    private DatabaseReference mDatabase;
    private List<OwnUUID> listData;

    AppDatabase localDb =null;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_your_status);
        enterPinTextBox=(EditText)findViewById(R.id.pass);
        Button shareStatusBtn = (Button) findViewById(R.id.push_ids);

//        localDb= Room.databaseBuilder(getApplicationContext(),
//                AppDatabase.class, "bandemic_database").build();
       // localDb=AppDatabase.getDatabase(getApplicationContext());
        mDatabase= FirebaseDatabase.getInstance().getReference().child("ListOfOwnUUID_Objects");

//        shareStatusBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String enteredPin= enterPinTextBox.getText().toString().trim();
//                if (enteredPin==("Covid")){
//
//                    postOwnIds();
//                   // startActivity(new Intent(ShareYourStatus.this, DataProtectionInfo.class));
//                }else{
//
//                    Toast.makeText(ShareYourStatus.this,"Test Result Code Incorrect...!",Toast.LENGTH_LONG).show();
//
//                }
//                finish();
//
//            }
//        });

        EnvironmentLoggerViewModel mViewModel = new ViewModelProvider(this).get(EnvironmentLoggerViewModel.class);
        // cardView.setCardBackgroundColor(getResources().getColor(R.color.colorNoDanger));

        // Create the observer which updates the UI.
        final Observer<List<OwnUUID>> nameObserver = ownUUIDS -> listData = ownUUIDS;


        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mViewModel.getAllOwnUUIDs().observe(this, nameObserver);


    }


    public OwnUUIDResponse createOwnUUIDResponse(){
        OwnUUIDResponse ids = new OwnUUIDResponse();
                        ids.setData(localDb.ownUUIDDao().getAll().getValue());
                        return ids;

    }
    public void postOwnIds() {
        //List<OwnUUID> data = new BroadcastRepository(getApplication()).getAllOwnUUIDs().getValue();
//        OwnUUIDResponse ownUUIDResponse = new OwnUUIDResponse(data);
//        OwnUUIDResponse ownUUIDResponse = createOwnUUIDResponse();
        OwnUUIDResponse response = new OwnUUIDResponse();
        response.setData(listData);
        mDatabase.push().setValue(response);

    }

    public void onShareYourStatus(View view) {
        String enteredPin= enterPinTextBox.getText().toString().trim();
        if (enteredPin.equalsIgnoreCase("Covid")){

            postOwnIds();
            // startActivity(new Intent(ShareYourStatus.this, DataProtectionInfo.class));
        }else{

            Toast.makeText(ShareYourStatus.this,"Test Result Code Incorrect...!",Toast.LENGTH_LONG).show();

        }
        finish();
    }


//    public void postOwnIds(OwnUUIDResponse ownUUIDResponse){
//
//Call<OwnUUIDResponse> OwnIdPostCall= RetrofitClient.postOwnIdsToWebservice().postOwnUUIDResponse(ownUUIDResponse);
//        OwnIdPostCall.enqueue(new Callback<OwnUUIDResponse>(){
//
//
//            @Override
//            public void onResponse(Call<OwnUUIDResponse> call, Response<OwnUUIDResponse> response) {
//                if(response.isSuccessful()){
//
//                    Toast.makeText(ShareYourStatus.this,"Shared Successfully!",Toast.LENGTH_LONG).show();
//                }else{
//
//                    Toast.makeText(ShareYourStatus.this,"Error...!",Toast.LENGTH_LONG).show();
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<OwnUUIDResponse> call, Throwable t) {
//                Toast.makeText(ShareYourStatus.this,"Error...!"+t.getLocalizedMessage(),Toast.LENGTH_LONG).show();
//
//            }
//        });
//    }


//    public void onShareStatusClick(View v) {
//        startActivity(new Intent(this, DataProtectionInfo.class));
//        finish();
//    }
}
