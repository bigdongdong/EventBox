package com.eventbox.cxd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import moudle.EventBox;
import moudle.EventBoxSubscribe;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBox.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBox.getDefault().unregister(this);
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        EventBox.getDefault().unregister(this);
//    }

    @EventBoxSubscribe
    public void getData(String s){
        Toast.makeText(this,"收到:String:"+s,Toast.LENGTH_SHORT).show();
        Log.i("EventBox", "收到:String: "+s);
    }

    public void goToSecondActivity(View view){
        this.startActivity(new Intent(this,SecondActivity.class));
    }
}
