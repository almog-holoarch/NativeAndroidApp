package com.unity.mynativeapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddRoll extends AppCompatActivity {

    private final String TAG = "HoloNAV ADD Roll Class TAG ";
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_roll);

        db = new Database();

        Toolbar toolbar = findViewById(R.id.rolls_toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle(R.string.TITLE_add_roll);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button add = findViewById(R.id.btn_add_roll);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String roll = ((EditText)findViewById(R.id.txt_add_roll_name)).getText().toString();

                if (db.isRollExists(roll)){
                    Toast.makeText(getApplicationContext(), getString(R.string.TOAST_roll_named) + " " + roll + " " + getString(R.string.TOAST_already_exists),Toast.LENGTH_SHORT).show();
                }

                else if(!db.rollIsValid(roll)){
                    Toast.makeText(getApplicationContext(),getString(R.string.TOAST_choose_roll),Toast.LENGTH_SHORT).show();
                }

                else {
                    db.addRollToDatabase(roll);
                    finish();
                }
            }
        });

    }
}