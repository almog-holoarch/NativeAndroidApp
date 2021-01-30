package com.unity.mynativeapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import static com.unity.mynativeapp.Database.getFromSubs;

public class EditSubstation extends AppCompatActivity {

    private static String TAG = "HoloNAV EDIT_SUB Class TAG ";

    private Database db;
    private Toolbar toolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_substation);

        db = new Database();

        toolbar = findViewById(R.id.edit_toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle(R.string.TITLE_edit_sub);
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

        final EditText x_offset = findViewById(R.id.x_offset_edit);
        final EditText y_offset = findViewById(R.id.y_offset_edit);
        final EditText z_offset = findViewById(R.id.z_offset_edit);
        final EditText x_rotate = findViewById(R.id.x_rotate_edit);
        final EditText y_rotate = findViewById(R.id.y_rotate_edit);
        final EditText z_rotate = findViewById(R.id.z_rotate_edit);

        final String old_x_offset = getFromSubs().getX_offset();
        final String old_y_offset = getFromSubs().getY_offset();
        final String old_z_offset = getFromSubs().getZ_offset();
        final String old_x_rotate = getFromSubs().getX_rotate();
        final String old_y_rotate = getFromSubs().getY_rotate();
        final String old_z_rotate = getFromSubs().getZ_rotate();

        nameEditText.setText(getFromSubs().getName());
        pathEditText.setText(getFromSubs().getPath());

        x_offset.setText(old_x_offset);
        y_offset.setText(old_y_offset);
        z_offset.setText(old_z_offset);
        x_rotate.setText(old_x_rotate);
        y_rotate.setText(old_y_rotate);
        z_rotate.setText(old_z_rotate);

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

                String new_x_offset = ((EditText)findViewById(R.id.x_offset_edit)).getText().toString();
                String new_y_offset = ((EditText)findViewById(R.id.y_offset_edit)).getText().toString();
                String new_z_offset = ((EditText)findViewById(R.id.z_offset_edit)).getText().toString();
                String new_x_rotate = ((EditText)findViewById(R.id.x_rotate_edit)).getText().toString();
                String new_y_rotate = ((EditText)findViewById(R.id.y_rotate_edit)).getText().toString();
                String new_z_rotate = ((EditText)findViewById(R.id.z_rotate_edit)).getText().toString();

                Substation edited_substation = new Substation(new_name, new_path, new_x_offset, new_y_offset, new_z_offset, new_x_rotate, new_y_rotate, new_z_rotate);

                if(!old_name.equals(new_name)
                        || !old_path.equals(new_path)
                        || !old_x_offset.equals(new_x_offset)
                        || !old_y_offset.equals(new_y_offset)
                        || !old_z_offset.equals(new_z_offset)
                        || !old_x_rotate.equals(new_x_rotate)
                        || !old_y_rotate.equals(new_y_rotate)
                        || !old_z_rotate.equals(new_z_rotate)){
                    if(!old_name.equals(new_name) && db.isNameExists(new_name)){
                        Toast.makeText(getApplicationContext(),getString(R.string.TOAST_substation_named)+ " " + new_name + " " + getString(R.string.TOAST_already_exists),Toast.LENGTH_SHORT).show();
                        nameEditText.setText("");
                    }

                    else if(!db.nameIsValid(new_name)){
                        Toast.makeText(getApplicationContext(),getString(R.string.TOAST_choose_name),Toast.LENGTH_SHORT).show();
                    }

                    else if(!db.pathIsValid(new_path)){
                        Toast.makeText(getApplicationContext(),getString(R.string.TOAST_upload_las),Toast.LENGTH_SHORT).show();
                    }

                    else{
                        db.editSubstationInDatabase(edited_substation, old_name, old_riskArea_file);
                        finish();
                    }
                }

                else{
                    finish();
                }
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
                builder.setMessage(getString(R.string.MESSAGE_are_you_sure) + "\n\n\"" + nameEditText.getText() + "\" " + getString(R.string.MESSAGE_will_be_delete))
                        .setPositiveButton(getString(R.string.BUTTON_yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.BUTTON_no), dialogClickListener)
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
