package com.eventbox.cxd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import moudle.EventBox;
import moudle.Subscribe;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


    @Subscribe
    public void getData(String s){
        Toast.makeText(this,"FirstActivity"+s,Toast.LENGTH_SHORT).show();
        Log.i("EventBox", "FirstActivity:String: "+s);
    }
    @Subscribe
    public void getData(Integer s){
        Toast.makeText(this,"FirstActivity"+s,Toast.LENGTH_SHORT).show();
        Log.i("EventBox", "FirstActivity:Integer: "+s);
    }
    @Subscribe
    public void getData(Float s){
        Toast.makeText(this,"FirstActivity"+s,Toast.LENGTH_SHORT).show();
        Log.i("EventBox", "FirstActivity: float:"+s);
    }

    @Subscribe
    public void getData(String[] s){
        Toast.makeText(this,"FirstActivity"+ JSON.toJSONString(s),Toast.LENGTH_SHORT).show();
        Log.i("EventBox", "FirstActivity: String[]:"+s);
    }
    @Subscribe
    public void getData(Double s){
        Toast.makeText(this,"FirstActivity"+ JSON.toJSONString(s),Toast.LENGTH_SHORT).show();
        Log.i("EventBox", "FirstActivity: double:"+s);
    }

    public void goToSecondActivity(View view){
        this.startActivity(new Intent(this,SecondActivity.class));
    }
}
