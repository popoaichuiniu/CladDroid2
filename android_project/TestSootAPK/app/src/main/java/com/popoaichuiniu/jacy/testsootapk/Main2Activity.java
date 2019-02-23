package com.popoaichuiniu.jacy.testsootapk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        sendSMS("18428376847","888");
        if(checkCallingPermission("android.permission.SEND_SMS")==PERMISSION_GRANTED)
        {
            sendSMS("18010823840","888");
        }
        else

        {
            Toast.makeText(this,"没有该权限",Toast.LENGTH_LONG).show();
        }


    }
    public void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        //smsManager.sendTextMessage("电话号码", null, "要发送的信息", null, null);
        smsManager.sendTextMessage(phoneNumber, null,message, null, null);
    }
}
