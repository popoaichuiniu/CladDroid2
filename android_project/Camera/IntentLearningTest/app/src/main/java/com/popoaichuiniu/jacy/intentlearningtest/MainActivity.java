package com.popoaichuiniu.jacy.intentlearningtest;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // Intent intent=new Intent();
//        intent.setClass(this,com.popoaichuiniu.jacy.getposition.Main2Activity.class);
        //intent.setClass(this,com.popoaichuiniu.jacy.intentlearningtest.LoginActivity.class);
        //startActivity(intent);

        // 获取包管理器
//        PackageManager manager = getPackageManager();
//        // 指定入口,启动类型,包名
//        Intent intent = new Intent(Intent.ACTION_MAIN);//入口Main
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);// 启动LAUNCHER,跟MainActivity里面的配置类似
//        intent.setPackage("com.tencent.mm");//包名
//        //查询要启动的Activity
//        List<ResolveInfo> apps = manager.queryIntentActivities(intent, 0);
//        if (apps.size() > 0) {//如果包名存在
//            ResolveInfo ri = apps.get(0);
//            // //获取包名
//            String packageName = ri.activityInfo.packageName;
//            //获取app启动类型
//            String className = ri.activityInfo.name;
//
//            Log.i("className",className);
//            //组装包名和类名
//            ComponentName cn = new ComponentName(packageName, "com.tencent.mm.ui.tools.ShareToTimeLineUI");
//            //设置给Intent
//            intent.setComponent(cn);
//            //根据包名类型打开Activity
//            startActivity(intent);
//        } else {
//            Toast.makeText(this, "找不到包名;" + "com.popoaichuiniu.jacy.getposition", Toast.LENGTH_SHORT).show();
//        }
    }
}
