package com.unity.mynativeapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "HoloNAV MainActivity Class TAG ";

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    static private TextView text_empty;
    private Database db;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {

            case R.id.action_manage_rolls:
                intent = new Intent(MainActivity.this, RollsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_contact_us:
                //intent = new Intent(MainActivity.this, ContactActivity.class);
                //startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        db = new Database();

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitle(R.string.TITLE_version);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // create APP's folder
        getApplicationContext().getExternalCacheDir();
        //////////////////////

        checkPermissions();

        RecyclerView recyclerView = findViewById(R.id.list);
        db.setUpSubstationsRecyclerView(this.getApplicationContext(), recyclerView);

        FloatingActionButton add = findViewById(R.id.MainFloatingActionButton);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddSubstation.class);
                startActivity(intent);
            }
        });

        text_empty = findViewById(R.id.emptyTextView);
    }

    protected void checkPermissions() {

        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, getString(R.string.TOAST_permission) + " '" + permissions[index]
                                + "' " + getString(R.string.TOAST_exiting), Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        db.updateSubstationsRecyclerView();
    }

    static public void displayEmptyListText(){
        text_empty.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        text_empty.setVisibility(View.VISIBLE);
    }

    static public void hideEmptyListText(){
        text_empty.setVisibility(View.GONE);
    }
}
