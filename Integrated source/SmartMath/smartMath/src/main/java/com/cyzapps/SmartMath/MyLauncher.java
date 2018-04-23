package com.cyzapps.SmartMath;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cyzapps.shapedetection.MainActivity;

public class MyLauncher extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_launcher);
    }
    public void go1(View v)
    {
        Intent i = new Intent(MyLauncher.this, ActivitySmartCalc.class);
        startActivity(i);
    }
    public void go2(View v)
    {
        Intent i = new Intent(MyLauncher.this,  com.cyzapps.shapedetection.MainActivity.class);
        startActivity(i);
    }
}
