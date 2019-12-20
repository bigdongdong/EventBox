package com.eventbox.cxd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cxd.eventbox.EventBox;
import com.cxd.eventbox.EventBoxSubscribe;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

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
        Toast.makeText(this,"收到:Stiring:"+s,Toast.LENGTH_SHORT).show();
        Log.i("EventBox", "收到:Stiring: "+s);
    }

    @EventBoxSubscribe
    public void getData(Integer s){
        Toast.makeText(this,"收到:Integer:"+s,Toast.LENGTH_SHORT).show();
        Log.i("EventBox", "收到:Integer: "+s);
    }

    public void goToThirdActivity(View view){
        this.startActivity(new Intent(this,ThirdActivity.class));
    }
}
