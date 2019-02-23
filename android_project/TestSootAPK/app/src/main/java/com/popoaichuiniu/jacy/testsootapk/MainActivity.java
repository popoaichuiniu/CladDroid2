package com.popoaichuiniu.jacy.testsootapk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    private int i= new Integer(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



//        if(getIntent().getBundleExtra("xxx").containsKey("xxxx")) {
//            sendSMS("","");
//        }


        //sendSMS("18010823840","777");

        Button button= (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(MainActivity.this,Main2Activity.class);
                startActivity(intent);
            }
        });


    }


    public void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        //smsManager.sendTextMessage("电话号码", null, "要发送的信息", null, null);
        smsManager.sendTextMessage(phoneNumber, null,message, null, null);
    }
}
