package app.bandemic.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import app.bandemic.R;

public class Instructions extends AppCompatActivity {
    Button next=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions);

    }

    public void onNextClick(View view) {
        startActivity(new Intent(this, DataProtectionInfo.class));
        finish();
    }
}
