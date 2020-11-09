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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static com.unity.mynativeapp.MainActivity.getFromSubs;

public class Edit extends AppCompatActivity {

    private static String TAG = "AlmogEditActivity";
    private Toolbar toolbar;

    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit);
        toolbar = (Toolbar) findViewById(R.id.edit_toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle(R.string.editTitle);
        setSupportActionBar(toolbar);
//      toolbar.inflateMenu(R.menu.send_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final String substationJsonPath = MainActivity.getSubstationJsonPath();

        final EditText nameEditText = findViewById(R.id.txt_edit_name);
        EditText pathEditText = findViewById(R.id.txt_edit_path);
        nameEditText.setText(getFromSubs().getName());
        pathEditText.setText(getFromSubs().getPath());

        final String oldName = nameEditText.getText().toString();
        final String oldPath = pathEditText.getText().toString();

        final File oldRiskAreaFile = new File(Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + "/" + oldName + ".json");

        Button upload = (Button) findViewById(R.id.btn_edit_upload);
        upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 7);
            }
        });

        Button save = (Button) findViewById(R.id.btn_edit_save);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                EditText newNameEditText = findViewById(R.id.txt_edit_name);
                EditText newPathEditText = findViewById(R.id.txt_edit_path);

                String newName = newNameEditText.getText().toString();
                String newPath = newPathEditText.getText().toString();

                Substation editedSubstation = new Substation(newName, newPath);
                ObjectMapper objectMapper = new ObjectMapper();
                File subsJsonFile = new File(substationJsonPath);

                Map<String, Substation> substationMap = new HashMap<>();;
                TypeReference<HashMap<String, Substation>> typeRef = new TypeReference<HashMap<String, Substation>>() {};

                try {
                    if(subsJsonFile.exists()){
                        substationMap = objectMapper.readValue(subsJsonFile, typeRef);

                        if(!oldName.equals(newName) ){
                            if(substationMap.containsKey(newName)){
                                Toast.makeText(getApplicationContext(),"Substation named " + newName + " already exists, please choose a new name",Toast.LENGTH_SHORT).show();
                                nameEditText.setText("");
                                return;
                            }
                        }
                    }

                    substationMap.remove(oldName);
                    substationMap.put(editedSubstation.getName(), editedSubstation);
                    objectMapper.writeValue(new File(substationJsonPath), substationMap);

                    // FIREBASE START //
//                    final DocumentReference docRef = db.collection("Substations").document(oldEvent.getDatabaseID());
//                    docRef.update("name", editedSubstation.getName());
//                    docRef.update("path", editedSubstation.getPath());
                    // FIREBASE END //

                    File newRiskAreaFile = new File(Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + "/" + newName + ".json");
                    oldRiskAreaFile.renameTo(newRiskAreaFile);

                } catch (IOException e) {
                    Log.d(TAG, "could not append json file while editing a substation because: " + e.getMessage());
                }

                finish();
            }
        });

        final Button delete = (Button) findViewById(R.id.btn_edit_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:

                                //Yes button clicked
                                ObjectMapper objectMapper = new ObjectMapper();
                                File jsonFile = new File(substationJsonPath);

                                Map<String, Substation> substationMap = new HashMap<>();;
                                TypeReference<HashMap<String, Substation>> typeRef = new TypeReference<HashMap<String, Substation>>() {};

                                try {
                                    if(jsonFile.exists()){
                                        substationMap = objectMapper.readValue(jsonFile, typeRef);

                                    }
                                    substationMap.remove(oldName);
                                    objectMapper.writeValue(new File(substationJsonPath), substationMap);
                                    oldRiskAreaFile.delete();

                                } catch (IOException e) {
                                    Log.d(TAG, "could not append json file while deleting a substation because: " + e.getMessage());
                                }

                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(delete.getContext());
                builder.setMessage("Are you sure ?\n\n\"" + nameEditText.getText() + "\" will be deleted permanently.").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){

            case 7:
                if(resultCode==RESULT_OK){
                    EditText txt = findViewById(R.id.txt_edit_path);
                    String p = FileUtils.getPath(getApplicationContext(), data.getData());

                    if(!p.endsWith(".las")){
                        Toast.makeText(getApplicationContext(),"Supports only .las file",Toast.LENGTH_SHORT).show();
                        txt.setText("");
                        return;
                    }
                    txt.setText(p);
                }
                break;
        }
    }


}
