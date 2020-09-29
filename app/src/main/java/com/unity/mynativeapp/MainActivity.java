package com.unity.mynativeapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "AlmogMainActivity";
    private TextView text_empty;
    private static int pos;
    private static String path;


    static private ArrayList<Substation> subs = new ArrayList<>();
    static private RecyclerView recyclerView;
    static private SubstationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.list);
        adapter = new SubstationsAdapter(subs);

        adapter.setOnItemClickListener(new SubstationsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {

                path = subs.get(position).getPath();

                if(path == ""){
                    Toast.makeText(getApplicationContext(),"Add a 3D model first",Toast.LENGTH_SHORT).show();
                } else if(!path.endsWith(".las") ){
                    Toast.makeText(getApplicationContext(),"supports .las file only",Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, MainUnityActivity.class);
                    startActivity(intent);
                    onUnityLoad(itemView);
                }
            }

            @Override
            public void onButtonClick(View itemView, int position) {
                pos = position;
                Intent intent = new Intent(MainActivity.this, Edit.class);
                startActivity(intent);
            }

        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button add = (Button) findViewById(R.id.btn_add);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Add.class);
                startActivity(intent);
            }
        });

        text_empty = findViewById(R.id.emptyTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

            File json = new File(getApplicationContext().getFilesDir() + "/" + "substations.json");

            if(json != null){
                Map<String, Substation> substationMap = new HashMap<>();

                try {

                    TypeReference<HashMap<String, Substation>> typeRef = new TypeReference<HashMap<String, Substation>>() {};
                    substationMap = objectMapper.readValue(json, typeRef);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(substationMap.size() >= 0){
                    subs.clear();

                    for (Map.Entry<String,Substation> entry : substationMap.entrySet()){
                        addToSubs(entry.getKey(), entry.getValue().getPath());
                    }

                } else{
                    Log.d(TAG,"Json file is empty");
                }

            } else{
                Log.d(TAG,"Cannot find a json file");
                removeFromSubs();
                adapter.notifyItemRemoved(pos);
            }

        if(subs.size() > 0)
        {
            text_empty.setVisibility(View.GONE);
        } else {
            text_empty.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            text_empty.setVisibility(View.VISIBLE);
        }
    }

    public static void addToSubs(String name, String path){
        subs.add(new Substation(name, path));
        adapter.notifyDataSetChanged();
    }

    public static void removeFromSubs(){
        subs.remove(pos);
        adapter.notifyItemRemoved(pos);
        adapter.notifyDataSetChanged();
    }

    public static Substation getFromSubs(){
        return subs.get(pos);
    }

    public static void setSubs(String name, String path){
        subs.get(pos).setName(name);
        subs.get(pos).setPath(path);
        adapter.notifyDataSetChanged();
    }

    public void onUnityLoad(View view) {
        Intent intent = new Intent(this, UnityCaller.class);
        startActivity(intent);
    }

    static public String getPath(){
        return path;
    }

}
