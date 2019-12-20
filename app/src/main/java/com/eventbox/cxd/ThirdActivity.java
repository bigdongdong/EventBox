package com.eventbox.cxd;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.cxd.eventbox.EventBox;

public class ThirdActivity extends AppCompatActivity implements View.OnClickListener {


    @BindView(R.id.buttonString)
    Button buttonString;
    @BindView(R.id.buttonInteger)
    Button buttonInteger;
    @BindView(R.id.buttonBoth)
    Button buttonBoth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        ButterKnife.bind(this);

        buttonString.setOnClickListener(this);
        buttonInteger.setOnClickListener(this);
        buttonBoth.setOnClickListener(this);
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
            case R.id.buttonString:
                EventBox.getDefault().send("爱你一万年", FirstActivity.class);
                break;
            case R.id.buttonInteger:
                EventBox.getDefault().send(2333, SecondActivity.class);
                break;
            case R.id.buttonBoth:
                EventBox.getDefault().send("爱你们一万年", FirstActivity.class ,SecondActivity.class);
                break;
        }
    }
}
