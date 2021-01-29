package com.unity.mynativeapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RollsActivity extends AppCompatActivity {

    private final String TAG = "HoloNAV Rolls Class TAG ";
    private Database db;
    static private TextView text_empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rolls);

        db = new Database();

        Toolbar toolbar = findViewById(R.id.rolls_toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle(R.string.TITLE_rollsManage);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.rollsList);
        db.setUpRollsRecyclerView(this.getApplicationContext(), recyclerView);

        FloatingActionButton add = findViewById(R.id.rollsFloatingActionButton);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(RollsActivity.this, AddRoll.class);
                startActivity(intent);
            }
        });

        text_empty = findViewById(R.id.emptyRollsListTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        db.updateRollsRecyclerView();
    }

    static public void displayEmptyListText(){
        text_empty.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        text_empty.setVisibility(View.VISIBLE);
    }

    static public void hideEmptyListText(){
        text_empty.setVisibility(View.GONE);
    }
}