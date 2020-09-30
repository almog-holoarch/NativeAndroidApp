package com.unity.mynativeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class Add extends AppCompatActivity {

    private final String TAG = "AlmogADD";
    private EditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

                //creating app's folder
                getApplicationContext().getExternalCacheDir();

                String substationJsonPath = MainActivity.getSubstationJsonPath();
                File subsJsonFile = new File(substationJsonPath);

                name = findViewById(R.id.txt_add_name);
                EditText path = findViewById(R.id.txt_add_path);
                String riskAreaJsonPath = Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + File.separator + name.getText() + ".json";
                File riskAreaJsonFile = new File(riskAreaJsonPath);

                // writing to a json file
                Substation addedSubstation = new Substation(name.getText().toString(), path.getText().toString());
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

                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){

            case 7:
                if(resultCode==RESULT_OK){
                    EditText txt = findViewById(R.id.txt_add_path);
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
