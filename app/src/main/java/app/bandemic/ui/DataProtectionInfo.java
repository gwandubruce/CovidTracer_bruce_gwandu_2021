package app.bandemic.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import app.bandemic.R;

public class DataProtectionInfo extends AppCompatActivity {
    Button ifPositive=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_protection_info);
        TextView tv = findViewById(R.id.dataProtectionContent);
        tv.setText(Html.fromHtml(getString(R.string.dataprotection_content)));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        ifPositive= findViewById(R.id.share);
        ifPositive.setOnClickListener(v -> {
//            Intent intent =new Intent(this,ShareYourStatus.class);
//            startActivity(intent);
            Intent i=new Intent(this,ShareYourStatus.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

        });
    }

    public void onOk(View view) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            editor.putBoolean(MainActivity.PREFERENCE_DATA_OK, true);
        }
        editor.apply();

        Intent i=new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);



    }
//    public void onStatusClick(View v) {
//        startActivity(new Intent(DataProtectionInfo.this, ShareYourStatus.class));
//        finish();
//    }

}
