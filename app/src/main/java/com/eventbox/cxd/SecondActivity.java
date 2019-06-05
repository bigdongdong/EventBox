package com.eventbox.cxd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.eventbox.cxd.moudle.eventbox.EventBox;
import com.eventbox.cxd.moudle.eventbox.Subscribe;

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

    @Subscribe
    public void getData(String s){
        Toast.makeText(this,"SecondActivity"+s,Toast.LENGTH_SHORT).show();
        Log.i("EventBox", "SecondActivity: "+s);
    }



    public void goToThirdActivity(View view){
        this.startActivity(new Intent(this,ThirdActivity.class));
    }
}
