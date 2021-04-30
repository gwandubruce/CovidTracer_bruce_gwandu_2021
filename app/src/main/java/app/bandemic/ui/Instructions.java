package app.bandemic.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import app.bandemic.R;

public class Instructions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions);

    }

    public void onNextClick(View view) {
        Intent i=new Intent(this,DataProtectionInfo.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
       // startActivity(new Intent(this, DataProtectionInfo.class));
       // finish();
    }
}
