package com.unity.mynativeapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static com.unity.mynativeapp.Database.getFromSubs;

public class Edit extends AppCompatActivity {

    private static String TAG = "HoloNAV EDIT Class TAG ";

    private Database db;
    private Toolbar toolbar;
    private final String substationJsonPath = Database.getSubstationJsonPath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit);

        db = new Database();

        toolbar = findViewById(R.id.edit_toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle(R.string.editTitle);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // load substation name & path
        final EditText nameEditText = findViewById(R.id.txt_edit_name);
        final EditText pathEditText = findViewById(R.id.txt_edit_path);
        nameEditText.setText(getFromSubs().getName());
        pathEditText.setText(getFromSubs().getPath());

        final String old_name = nameEditText.getText().toString();
        final String old_path = pathEditText.getText().toString();
        final File old_riskArea_file = new File(Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + "/" + old_name + ".json");

        Button upload = findViewById(R.id.btn_edit_upload);
        upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 7);
            }
        });

        Button save = findViewById(R.id.btn_edit_save);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String new_name = ((EditText)findViewById(R.id.txt_edit_name)).getText().toString();
                String new_path = ((EditText)findViewById(R.id.txt_edit_path)).getText().toString();

                Substation edited_substation = new Substation(new_name, new_path);

                if(!old_name.equals(new_name)){
                    if(db.isNameExists(new_name)){
                        Toast.makeText(getApplicationContext(),"Substation named " + new_name + " already exists, please choose a new name",Toast.LENGTH_SHORT).show();
                        nameEditText.setText("");
                        return;
                    }
                }

                if(!old_path.equals(new_path) || !old_name.equals(new_name)){
                    db.editSubstationInDatabase(edited_substation, old_name, old_riskArea_file);
                }

                finish();

            }
        });

        final Button delete = findViewById(R.id.btn_edit_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:

                                //Yes button clicked
                                db.removeSubstationFromDatabase(old_name, old_riskArea_file);

                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(delete.getContext());
                builder.setMessage("Are you sure ?\n\n\"" + nameEditText.getText() + "\" will be deleted permanently.")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case 7:
                if (resultCode == RESULT_OK) {
                    EditText txt = findViewById(R.id.txt_edit_path);
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
