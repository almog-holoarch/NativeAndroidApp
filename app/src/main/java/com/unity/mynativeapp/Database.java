package com.unity.mynativeapp;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static com.unity.mynativeapp.MainUnityActivity.updateRiskAreaJsonPath;

public class Database extends AppCompatActivity {

    private final String TAG = "HoloNAV Database Class TAG ";

    private static String substation_Json_Path = Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + File.separator + "substations.json";

    ObjectMapper objectMapper = new ObjectMapper();
    File subsJsonFile = new File(substation_Json_Path);

    Map<String, Substation> substationMap = new HashMap<>();;
    TypeReference<HashMap<String, Substation>> typeRef = new TypeReference<HashMap<String, Substation>>() {};

    static private ArrayList<Substation> subs = new ArrayList<>();
    static private SubstationsAdapter adapter;

    private static int pos;
    private static String path;

    public Database() { }

    public boolean isNameExists(String name) {

        try {
            if(subsJsonFile.exists()){
                substationMap = objectMapper.readValue(subsJsonFile, typeRef);

                if(substationMap.containsKey(name)){

                    return true;
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "problem with checking if substation name already exists: " + e.getMessage());
        }

        return false;
    }

    public void addSubstationToDatabase(Substation new_sub){

        File subsJsonFile = new File(substation_Json_Path);

        String riskAreaJsonPath = Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + File.separator + new_sub.getName() + ".json";
        File riskAreaJsonFile = new File(riskAreaJsonPath);

        Log.d(TAG, "path 1 : " + substation_Json_Path);
        Log.d(TAG, "path 2 : " + riskAreaJsonPath);

        // writing to a json file
        Substation addedSubstation = new Substation(new_sub.getName(), new_sub.getPath(), new_sub.getX_offset(), new_sub.getY_offset(), new_sub.getZ_offset(), new_sub.getX_rotate(), new_sub.getY_rotate(), new_sub.getZ_rotate());
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
                    Toast.makeText(getApplicationContext(),getString(R.string.TOAST_substation_named)+ " " + addedSubstation.getName() + " " + getString(R.string.TOAST_already_exists),Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            substationMap.put(addedSubstation.getName(), addedSubstation);
            objectMapper.writeValue(new File(substation_Json_Path), substationMap);
        } catch (IOException e) {
            Log.d(TAG, "could not append json file because: " + e.getMessage());
        }
    }

    boolean nameIsValid(String name){
        return !name.equals("");
    }

    boolean pathIsValid(String path){
        return !path.equals("");
    }

    public void editSubstationInDatabase(Substation edited_substation, String old_name, File old_riskArea_file){

        try {

            substationMap.remove(old_name);
            substationMap.put(edited_substation.getName(), edited_substation);
            objectMapper.writeValue(new File(substation_Json_Path), substationMap);

            File newRiskAreaFile = new File(Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + "/" + edited_substation.getName() + ".json");
            old_riskArea_file.renameTo(newRiskAreaFile);

        } catch (IOException e) {
            Log.d(TAG, "could not append json file while editing a substation because: " + e.getMessage());
        }
    }

    public void removeSubstationFromDatabase(String old_name, File old_riskArea_file) {

        try {

            if(subsJsonFile.exists()){
                substationMap = objectMapper.readValue(subsJsonFile, typeRef);
            }

            substationMap.remove(old_name);
            objectMapper.writeValue(new File(substation_Json_Path), substationMap);
            old_riskArea_file.delete();

            adapter.notifyItemRemoved(pos);
            adapter.notifyDataSetChanged();

        } catch (IOException e) {
            Log.d(TAG, "could not append json file while deleting a substation because: " + e.getMessage());
        }
    }

    public void setUpSubstationsRecyclerView(final Context context, RecyclerView recyclerView){

        sortList();
        adapter = new SubstationsAdapter(subs);
        adapter.setOnItemClickListener(new SubstationsAdapter.OnItemClickListener() {

            //Click a substation from substations list
            @Override
            public void onItemClick(View itemView, int position) {

                path = subs.get(position).getPath();

                if(path == ""){
                    Toast.makeText(itemView.getContext(),getString(R.string.TOAST_upload_las),Toast.LENGTH_SHORT).show();
                } else if(!path.endsWith(".las") ){
                    Toast.makeText(itemView.getContext(),getString(R.string.TOAST_support_las),Toast.LENGTH_SHORT).show();
                } else {
                    String nodeName = subs.get(position).getName();
                    String riskAreaJsonPath = Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + File.separator + nodeName + ".json";
                    Log.d(TAG, "risk area path is: " + riskAreaJsonPath);
                    updateRiskAreaJsonPath(riskAreaJsonPath);
                    Intent intent = new Intent(itemView.getContext(), MainUnityActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            }

            // click edit button on a substation
            @Override
            public void onButtonClick(View itemView, int position) {
                pos = position;
                Intent intent = new Intent(itemView.getContext(), Edit.class);
                itemView.getContext().startActivity(intent);
            }

        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

    }

    private static void sortList(){
        Collections.sort(subs, new Comparator<Substation>() {
            @Override
            public int compare(Substation lhs, Substation rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
    }

    public void updateSubstationsRecyclerView(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        File json = new File(substation_Json_Path);

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
                    addToSubs(entry.getKey(), entry.getValue().getPath(), entry.getValue().getX_offset(), entry.getValue().getY_offset(), entry.getValue().getZ_offset(), entry.getValue().getX_rotate(), entry.getValue().getY_rotate(), entry.getValue().getZ_rotate());
                }

            } else{
                Log.d(TAG,"Json file is empty");
            }

        } else{
            Log.d(TAG,"Cannot find a json file");
            removeFromSubs();
        }

        if(subs.size() > 0)
        {
            MainActivity.hideEmptyListText();
        } else {
            MainActivity.displayEmptyListText();
        }
    }

    public static void addToSubs(String name, String path, String xo, String yo, String zo, String xr, String yr, String zr){
        subs.add(new Substation(name, path, xo, yo, zo, xr, yr, zr));
        sortList();
        adapter.notifyDataSetChanged();
    }

    public static void removeFromSubs(){
        subs.remove(pos);
        sortList();
        adapter.notifyItemRemoved(pos);
        adapter.notifyDataSetChanged();
    }

    public static Substation getFromSubs(){
        return subs.get(pos);
    }

    static public String getPath(){
        return path;
    }

    static public String getSubstationJsonPath(){
        return substation_Json_Path;
    }
}
