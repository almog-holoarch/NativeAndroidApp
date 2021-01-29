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
import android.util.Log;
import java.io.File;
import static com.unity.mynativeapp.Database.getFromSubs;

public class Edit extends AppCompatActivity {

    private static String TAG = "HoloNAV EDIT Class TAG ";

    private Database db;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit);

        db = new Database();

        toolbar = findViewById(R.id.edit_toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle(R.string.TITLE_editTitle);
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

        final EditText xo = findViewById(R.id.x_offset_edit);
        final EditText yo = findViewById(R.id.y_offset_edit);
        final EditText zo = findViewById(R.id.z_offset_edit);
        final EditText xr = findViewById(R.id.x_rotate_edit);
        final EditText yr = findViewById(R.id.y_rotate_edit);
        final EditText zr = findViewById(R.id.z_rotate_edit);

        String xos = getFromSubs().getX_offset();
        String yos = getFromSubs().getY_offset();
        String zos = getFromSubs().getZ_offset();
        String xrs = getFromSubs().getX_rotate();
        String yrs = getFromSubs().getY_rotate();
        String zrs = getFromSubs().getZ_rotate();

        nameEditText.setText(getFromSubs().getName());
        pathEditText.setText(getFromSubs().getPath());

        xo.setText(xos);
        yo.setText(yos);
        zo.setText(zos);
        xr.setText(xrs);
        yr.setText(yrs);
        zr.setText(zrs);

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

                String new_xo = ((EditText)findViewById(R.id.x_offset_edit)).getText().toString();
                String new_yo = ((EditText)findViewById(R.id.y_offset_edit)).getText().toString();
                String new_zo = ((EditText)findViewById(R.id.z_offset_edit)).getText().toString();
                String new_xr = ((EditText)findViewById(R.id.x_rotate_edit)).getText().toString();
                String new_yr = ((EditText)findViewById(R.id.y_rotate_edit)).getText().toString();
                String new_zr = ((EditText)findViewById(R.id.z_rotate_edit)).getText().toString();

                Substation edited_substation = new Substation(new_name, new_path, new_xo, new_yo, new_zo, new_xr, new_yr, new_zr);

                if(!old_name.equals(new_name) || !old_path.equals(new_path)){
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
