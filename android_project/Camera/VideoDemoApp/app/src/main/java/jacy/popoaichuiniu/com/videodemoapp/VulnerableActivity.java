package jacy.popoaichuiniu.com.videodemoapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;

public class VulnerableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vulnerable);
        int choice = new Integer(5);
        if (choice == 5)//
        {
            if (getIntent().getBundleExtra("bundle").getString("bundleKey").equals("bundleData"))
            {
                SmsManager.getDefault().sendTextMessage("123456789", null, getIntent().getStringExtra("content"), null, null);
            }
        }
    }

}
