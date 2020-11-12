package com.unity.mynativeapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Add extends AppCompatActivity {

    private final String TAG = "HoloNAV ADD Class TAG ";

    private Database db;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add);

        db = new Database();

        toolbar = findViewById(R.id.add_toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle(R.string.addTitle);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button upload = findViewById(R.id.btn_add_upload);
        upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 7);
            }
        });

        Button accept = findViewById(R.id.btn_add_accept);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String name = ((EditText)findViewById(R.id.txt_add_name)).getText().toString();
                String path = ((EditText)findViewById(R.id.txt_add_path)).getText().toString();

                if (db.isNameExists(name)){
                    Toast.makeText(getApplicationContext(),"Name already exists",Toast.LENGTH_SHORT).show();
                }

                else if(!nameIsValid(name)){
                    Toast.makeText(getApplicationContext(),"Choose A substation name",Toast.LENGTH_SHORT).show();
                }

                else if(!pathIsValid(path)){
                    Toast.makeText(getApplicationContext(),"Upload A substation .las file",Toast.LENGTH_SHORT).show();
                }

                else {
                    Substation new_sub = new Substation(name, path);
                    db.addSubstationToDatabase(new_sub);
                    finish();
                }
            }
        });
    }

    boolean nameIsValid(String name){
        return !name.equals("");
    }

    boolean pathIsValid(String path){
        return !path.equals("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case 7:
                if (resultCode == RESULT_OK) {
                    EditText txt = findViewById(R.id.txt_add_path);
                    String p = FileUtils.getPath(getApplicationContext(), data.getData());

                    if (!p.endsWith(".las")) {
                        Toast.makeText(getApplicationContext(), "Supports only .las file", Toast.LENGTH_SHORT).show();
                        txt.setText("");
                        return;
                    }
                    txt.setText(p);
                }
                break;
        }
    }

}
