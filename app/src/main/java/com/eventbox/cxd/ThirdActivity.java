package com.eventbox.cxd;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import moudle.EventBox;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ThirdActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.button1)
    Button button1;
    @BindView(R.id.button2)
    Button button2;
    @BindView(R.id.button3)
    Button button3;
    @BindView(R.id.button4)
    Button button4;
    @BindView(R.id.button5)
    Button button5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        ButterKnife.bind(this);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
    }

    public void sendSecondActivity(View view) {
        EventBox.getDefault().send("爱你一万年", SecondActivity.class);
    }

    public void sendBothActivity(View view) {
        EventBox.getDefault().send("爱你一万年", FirstActivity.class, SecondActivity.class);
    }

    public void gotoFirstActivity(View view) {
        this.startActivity(new Intent(this, FirstActivity.class));
    }

    public void gotoSecondActivity(View view) {
        this.startActivity(new Intent(this, SecondActivity.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                EventBox.getDefault().send(new String("来自ThirdActivity的问候"), FirstActivity.class);
                break;
            case R.id.button2:
                EventBox.getDefault().send(new String[]{"来自", "ThirdActivity", "的问候"}, FirstActivity.class);
                break;
            case R.id.button3:
                EventBox.getDefault().send(3.3f, FirstActivity.class);
                break;
            case R.id.button4:
                EventBox.getDefault().send(211, FirstActivity.class);
                break;
            case R.id.button5:
                EventBox.getDefault().send(3.3333333333333, FirstActivity.class);
                break;
        }
    }
}
