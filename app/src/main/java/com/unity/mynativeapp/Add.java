package com.unity.mynativeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Add extends AppCompatActivity {

    private final String TAG = "AlmogADD";
    private EditText editText_name;

    private Toolbar toolbar;

    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add);
        toolbar = (Toolbar) findViewById(R.id.add_toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle(R.string.addTitle);
        setSupportActionBar(toolbar);
//      toolbar.inflateMenu(R.menu.send_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button upload = (Button) findViewById(R.id.btn_add_upload);
        upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 7);
            }
        });

        Button accept = (Button) findViewById(R.id.btn_add_accept);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                editText_name = findViewById(R.id.txt_add_name);
                Editable name = editText_name.getText();

                EditText editText_path = findViewById(R.id.txt_add_path);
                Editable path = editText_path.getText();

                if(name.toString().equals("")){
                    Toast.makeText(getApplicationContext(),"Choose A substation name",Toast.LENGTH_SHORT).show();
                }

                else if(path.toString().equals("")){
                    Toast.makeText(getApplicationContext(),"Upload A substation .las file",Toast.LENGTH_SHORT).show();
                }

                else {
                    //creating app's folder
                    getApplicationContext().getExternalCacheDir();

                    String substationJsonPath = MainActivity.getSubstationJsonPath();
                    File subsJsonFile = new File(substationJsonPath);

                    String riskAreaJsonPath = Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + File.separator + name + ".json";
                    File riskAreaJsonFile = new File(riskAreaJsonPath);

                    // writing to a json file
                    Substation addedSubstation = new Substation(name.toString(), path.toString());
                    ObjectMapper objectMapper = new ObjectMapper();

                    //creating a JSON file for the new added substation
                    try {
                        riskAreaJsonFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Map<String, Substation> substationMap = new HashMap<>();
                    TypeReference<HashMap<String, Substation>> typeRef = new TypeReference<HashMap<String, Substation>>() {};

                    try {
                        if(subsJsonFile.exists()){
                            substationMap = objectMapper.readValue(subsJsonFile, typeRef);
                            if(substationMap.containsKey(addedSubstation.getName())){
                                Toast.makeText(getApplicationContext(),"Substation named " + addedSubstation.getName() + " already exists",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        substationMap.put(addedSubstation.getName(), addedSubstation);
                        objectMapper.writeValue(new File(substationJsonPath), substationMap);
                    } catch (IOException e) {
                        Log.d(TAG, "could not append json file because: " + e.getMessage());
                    }

                    // FIREBASE START //
                    // Add a new document with a generated ID
                    db.collection("Substations")
                            .add(addedSubstation)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });
                    // FIREBASE END //

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
