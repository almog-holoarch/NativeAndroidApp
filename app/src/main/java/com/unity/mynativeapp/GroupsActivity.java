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

public class GroupsActivity extends AppCompatActivity {

    private final String TAG = "HoloNAV Rolls Class TAG ";
    private Database db;
    static private TextView text_empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        db = new Database();

        Toolbar toolbar = findViewById(R.id.groups_toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle(R.string.TITLE_groupsManage);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.groupsList);
        db.setUpRollsRecyclerView(this.getApplicationContext(), recyclerView);

        FloatingActionButton add = findViewById(R.id.groupsFloatingActionButton);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(GroupsActivity.this, AddGroup.class);
                startActivity(intent);
            }
        });

        text_empty = findViewById(R.id.emptyGroupsListTextView);
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