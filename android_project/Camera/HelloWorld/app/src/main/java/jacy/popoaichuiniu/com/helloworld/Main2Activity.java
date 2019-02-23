package jacy.popoaichuiniu.com.helloworld;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Log.i("mmm","create2");
        Button button1=findViewById(R.id.button1);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClassName("jacy.popoaichuiniu.com.hellworldb","jacy.popoaichuiniu.com.hellworldb.MainActivity");
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i("mmm","start2");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("mmm","onResume2");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("mmm","onPause2");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("mmm","onStop2");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("mmm","onDestroy2");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("mmm","onRestart2");
    }
}
