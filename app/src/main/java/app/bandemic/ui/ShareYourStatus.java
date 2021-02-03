package app.bandemic.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import app.bandemic.R;
import app.bandemic.strict.database.AppDatabase;
import app.bandemic.strict.network.OwnUUIDResponse;
import app.bandemic.strict.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShareYourStatus extends AppCompatActivity {
    EditText enterPinTextBox=null;
    Button shareStatusBtn=null;

    AppDatabase db =null;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_your_status);
        enterPinTextBox=(EditText)findViewById(R.id.pin);
        shareStatusBtn=(Button)findViewById(R.id.push_ids);
        String enteredPin= enterPinTextBox.getText().toString().trim();
        db= Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "bandemic_database").build();

        shareStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (enteredPin==("Covid")){

                    postOwnIds(createOwnUUIDResponse());
                   // startActivity(new Intent(ShareYourStatus.this, DataProtectionInfo.class));
                }else{

                    Toast.makeText(ShareYourStatus.this,"Test Result Code Incorrect...!",Toast.LENGTH_LONG).show();

                }
                finish();

            }
        });

    }

    public OwnUUIDResponse createOwnUUIDResponse(){
        OwnUUIDResponse ids = new OwnUUIDResponse();
                        ids.setData(db.ownUUIDDao().getAll().getValue());
                        return ids;

    }


    public void postOwnIds(OwnUUIDResponse ownUUIDResponse){

Call<OwnUUIDResponse> OwnIdPostCall= RetrofitClient.postOwnIdsToWebservice().postOwnUUIDResponse(ownUUIDResponse);
        OwnIdPostCall.enqueue(new Callback<OwnUUIDResponse>(){


            @Override
            public void onResponse(Call<OwnUUIDResponse> call, Response<OwnUUIDResponse> response) {
                if(response.isSuccessful()){

                    Toast.makeText(ShareYourStatus.this,"Shared Successfully!",Toast.LENGTH_LONG).show();
                }else{

                    Toast.makeText(ShareYourStatus.this,"Error...!",Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<OwnUUIDResponse> call, Throwable t) {
                Toast.makeText(ShareYourStatus.this,"Error...!"+t.getLocalizedMessage(),Toast.LENGTH_LONG).show();

            }
        });
    }

//    public void onShareStatusClick(View v) {
//        startActivity(new Intent(this, DataProtectionInfo.class));
//        finish();
//    }
}
