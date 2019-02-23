package com.popoaichuiniu.jacy.devicepermissionapply;

import android.app.Instrumentation;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    /**
     * 设备管理员
     */
    private DevicePolicyManager mDPM;
    /**
     * 四大组件名的封装类
     */
    private ComponentName mConmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 1, 获取设备管理员
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        // 2, 申请权限
        mConmp = new ComponentName(this, MyReceiver.class);

        Button lockScreen= (Button) findViewById(R.id.button);
        lockScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                policy(v);
            }
        });

    }
    // 点击按钮去锁屏
    public void policy(View v) {

        // 判断是否获得管理员权限啊
        boolean active = mDPM.isAdminActive(mConmp);

        if (active) {
            // 已经获取管理员权限可以锁屏
            System.out.println("--已经获取管理员权限--");
            mDPM.lockNow();
//            // 解锁时要输入123才能解锁
//            mDPM.resetPassword("123", 0);

        } else {
            // 没有管理员权限---启动系统activity让用户激活管理员权限
            Intent intent = new Intent(
                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mConmp);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "只有激活了管理员权限才能锁屏,清理缓存");
            startActivityForResult(intent, 0);

        }

    }

}
