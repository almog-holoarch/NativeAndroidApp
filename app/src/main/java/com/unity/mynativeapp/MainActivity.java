package com.unity.mynativeapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.unity.mynativeapp.MainUnityActivity.updateRiskAreaJsonPath;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "AlmogMainActivity";

    private RecyclerView recyclerV;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter substationsFBAdapter;

    private static String substationJsonPath = Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + File.separator + "substations.json";
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };

    private TextView text_empty;
    private static int pos;
    private static String path;

    static private ArrayList<Substation> subs = new ArrayList<>();
    static private RecyclerView recyclerView;
    static private SubstationsAdapter adapter;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle(R.string.version);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        checkPermissions();

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
                    String nodeName = subs.get(position).getName();
                    String riskAreaJsonPath = Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + File.separator + nodeName + ".json";
                    Log.d(TAG, "riskareapath is: " + riskAreaJsonPath);
                    updateRiskAreaJsonPath(riskAreaJsonPath);
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

        //  FIREBASE START //
//        Query query = FirebaseFirestore.getInstance().collection("Substations").orderBy("name", Direction.DESCENDING).limit(50);
//        FirestoreRecyclerOptions<Substation> options =
//                new FirestoreRecyclerOptions
//                        .Builder<Substation>()
//                        .setQuery(query, Substation.class)
//                        .build();
//        recyclerV = recyclerView;
//
//        substationsFBAdapter = new FirestoreRecyclerAdapter<Substation, node>(options){
//
//            @Override
//            public SubHolder onCreateViewHolder(ViewGroup parent,
//                                                  int viewType) {
//
//                View v = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.node, parent, false);
//
//                return  new SubHolder(v);
//            }
//
//            @Override
//            public void onBindViewHolder(DriveHolder holder, int position, final Drive item) {
//                holder.setDriveFromLocation(item.getFromCity());
//                holder.setDriveTime(item.getTime());
//                holder.setDriveToLocation(item.getToCity());
//                holder.setDriverID(item.getDriverFullName());
//                holder.setDriversProfilePic((Uri.parse(item.getDriverProfilePicUri())));
//                holder.itemView.setOnClickListener( new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(v.getContext(), DisplayRideActivity.class);
//                        intent.putExtra("drive", item);
//                        v.getContext().startActivity(intent);
//                    }
//                });
//            }
//        };
//
//        layoutManager = new LinearLayoutManager(context);
//        recyclerV.setLayoutManager(layoutManager);
//        recyclerV.setAdapter(ridesFBAdapter);
//        ridesFBAdapter.startListening();
        // FIREBASE END //

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton add = (FloatingActionButton)  findViewById(R.id.floatingActionButton);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Add.class);
                startActivity(intent);
            }
        });

        text_empty = findViewById(R.id.emptyTextView);
    }


    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                }

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

            File json = new File(substationJsonPath);

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

    static public String getSubstationJsonPath(){
        return substationJsonPath;
    }
}
