package com.unity.mynativeapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.unity.mynativeapp.Database.getFromGroups;

public class EditGroup extends AppCompatActivity {

    private final String TAG = "HoloNAV EDIT Roll Class TAG ";
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        db = new Database();

        Toolbar toolbar = findViewById(R.id.groups_toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle(R.string.TITLE_edit_group);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final EditText nameEditText = findViewById(R.id.txt_edit_group_name);
        nameEditText.setText(getFromGroups().getName());
        final String old_name = nameEditText.getText().toString();

        Button save = findViewById(R.id.btn_edit_group_save);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String new_name = ((EditText)findViewById(R.id.txt_edit_group_name)).getText().toString();
                Group edited_group = new Group(new_name);

                if(!old_name.equals(new_name)){

                    if(!old_name.equals(new_name) && db.isRollExists(new_name)){
                        Toast.makeText(getApplicationContext(),getString(R.string.TOAST_group_named)+ " " + new_name + " " + getString(R.string.TOAST_already_exists),Toast.LENGTH_SHORT).show();
                        nameEditText.setText("");
                    }

                    else if(!db.groupIsValid(new_name)){
                        Toast.makeText(getApplicationContext(),getString(R.string.TOAST_choose_group),Toast.LENGTH_SHORT).show();
                    }

                    else{
                        db.editGroupInDatabase(edited_group, old_name);
                        finish();
                    }
                }

                else{
                    finish();
                }
            }
        });

        final Button delete = findViewById(R.id.btn_edit_group_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:

                                //Yes button clicked
                                db.removeRollFromDatabase(old_name);

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
}