package com.eventbox.cxd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.eventbox.cxd.moudle.eventbox.EventBox;

public class ThirdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);


    }

    public void sendFirstActivity(View view){
        EventBox.getDefault().send(FirstActivity.class,new String[]{"sds","sss","cccc"});
    }
    public void sendSecondActivity(View view){
        EventBox.getDefault().send(SecondActivity.class,"爱你一万年");
    }
    public void sendBothActivity(View view){
        EventBox.getDefault().send(FirstActivity.class,"爱你一万年");
        EventBox.getDefault().send(SecondActivity.class,"爱你一万年");
    }

    public void gotoFirstActivity(View view){
        this.startActivity(new Intent(this,FirstActivity.class));
    }
    public void gotoSecondActivity(View view){
        this.startActivity(new Intent(this,SecondActivity.class));
    }
}
