package com.unity.mynativeapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class UnityCaller extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getApplicationContext(), MainUnityActivity.class);
        startActivity(intent);
    }
}
