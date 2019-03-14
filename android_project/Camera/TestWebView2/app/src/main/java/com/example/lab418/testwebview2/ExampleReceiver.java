package com.example.lab418.testwebview2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class ExampleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String xxx="rrr";
        int x=new Integer(2);
        String sss=String.valueOf(x);

        String ttt=sss+xxx;
        if(intent.getStringExtra("ggg").equals(ttt))
        {
            SmsManager.getDefault().sendTextMessage("123456789", null, "fff", null, null);
        }

    }
}
