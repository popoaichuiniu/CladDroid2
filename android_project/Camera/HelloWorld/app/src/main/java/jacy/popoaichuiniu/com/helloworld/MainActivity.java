package jacy.popoaichuiniu.com.helloworld;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button=findViewById(R.id.button);
        Log.i("mmm","create");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(MainActivity.this,Main2Activity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("mmm","start");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("mmm","onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("mmm","onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("mmm","onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("mmm","onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("mmm","onRestart");
    }
}
