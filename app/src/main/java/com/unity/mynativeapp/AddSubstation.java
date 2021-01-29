package com.unity.mynativeapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddSubstation extends AppCompatActivity {

    private final String TAG = "HoloNAV ADD Class TAG ";

    private Database db;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_substation);

        db = new Database();

        toolbar = findViewById(R.id.add_toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle(R.string.TITLE_add_sub);
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

        Button accept = findViewById(R.id.btn_add_sub);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String name = ((EditText)findViewById(R.id.txt_add_name)).getText().toString();
                String path = ((EditText)findViewById(R.id.txt_add_path)).getText().toString();

                String xo = ((EditText)findViewById(R.id.x_offset)).getText().toString();
                String yo = ((EditText)findViewById(R.id.y_offset)).getText().toString();
                String zo = ((EditText)findViewById(R.id.z_offset)).getText().toString();
                String xr = ((EditText)findViewById(R.id.x_rotate)).getText().toString();
                String yr = ((EditText)findViewById(R.id.y_rotate)).getText().toString();
                String zr = ((EditText)findViewById(R.id.z_rotate)).getText().toString();

                if (db.isNameExists(name)){
                    Toast.makeText(getApplicationContext(), getString(R.string.TOAST_substation_named) + " " + name + " " + getString(R.string.TOAST_already_exists),Toast.LENGTH_SHORT).show();
                }

                else if(!db.nameIsValid(name)){
                    Toast.makeText(getApplicationContext(),getString(R.string.TOAST_choose_name),Toast.LENGTH_SHORT).show();
                }

                else if(!db.pathIsValid(path)){
                    Toast.makeText(getApplicationContext(),getString(R.string.TOAST_upload_las),Toast.LENGTH_SHORT).show();
                }

                else {
                    Substation new_sub = new Substation(name, path, xo, yo, zo, xr, yr, zr);
                    db.addSubstationToDatabase(new_sub);
                    finish();
                }
            }
        });
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
                        Toast.makeText(getApplicationContext(), getString(R.string.TOAST_support_las), Toast.LENGTH_SHORT).show();
                        txt.setText("");
                        return;
                    }
                    txt.setText(p);
                }
                break;
        }
    }

}
