package com.example.lab418.testwebview2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class ExampleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String xxx = "rrr";
        int x = new Integer(2);
        String sss = String.valueOf(x);

        String ttt = sss + xxx;

        if (intent.getIntExtra("ggg",-1)-7>0) {
            Log.i("ZMSGetInfo","success!");
           Toast.makeText(context,"2222222222",Toast.LENGTH_LONG).show();
            //if (intent.getAction().equals(ttt)) {
                //SmsManager.getDefault().sendTextMessage("123456789", null, "fff", null, null);
           // }

        }

    }
}
