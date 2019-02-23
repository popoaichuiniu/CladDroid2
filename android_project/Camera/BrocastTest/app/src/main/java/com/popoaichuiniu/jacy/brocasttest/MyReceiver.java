package com.popoaichuiniu.jacy.brocasttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Toast.makeText(context,"我被启动啦！",Toast.LENGTH_LONG).show();
//        Intent intent1=new Intent();
//        intent1.setClass(context,MainActivity.class);
//
//        context.startActivity(intent1);

        //Log.i("haha","xxx");
//        Bundle xxx=intent.getBundleExtra("xxx");
//        if(xxx.containsKey("xxx"))
//        {
//            Log.i("haha","xxx");
//        }

    }
}
