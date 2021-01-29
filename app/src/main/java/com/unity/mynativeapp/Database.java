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

    ObjectMapper objectMapper = new ObjectMapper();

    public Database() { }

    //////////////////////
    //  Substations     //
    //////////////////////

    private static String substation_Json_Path = Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + File.separator + "substations.json";
    File subsJsonFile = new File(substation_Json_Path);
    Map<String, Substation> substationMap = new HashMap<>();;
    TypeReference<HashMap<String, Substation>> typeRef = new TypeReference<HashMap<String, Substation>>() {};
    static private ArrayList<Substation> subs = new ArrayList<>();
    static private SubstationsAdapter subs_adapter;
    private static int pos;
    private static String path;

    //GET_PATH
    static public String getPath(){
        return path;
    }

    //GET_SUBS_JSON_PATH
    static public String getSubstationJsonPath(){
        return substation_Json_Path;
    }

    //SORT_SUBS_LIST
    private static void sortSubsList(){
        Collections.sort(subs, new Comparator<Substation>() {
            @Override
            public int compare(Substation lhs, Substation rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
    }

    //ADD
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

        //Map<String, Substation> substationMap = new HashMap<>();
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
            Log.d(TAG, "could not append subs json file because: " + e.getMessage());
        }
    }

    //EDIT
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

    //REMOVE
    public void removeSubstationFromDatabase(String old_name, File old_riskArea_file) {

        try {

            if(subsJsonFile.exists()){
                substationMap = objectMapper.readValue(subsJsonFile, typeRef);
            }

            substationMap.remove(old_name);
            objectMapper.writeValue(new File(substation_Json_Path), substationMap);
            old_riskArea_file.delete();

            subs_adapter.notifyItemRemoved(pos);
            subs_adapter.notifyDataSetChanged();

        } catch (IOException e) {
            Log.d(TAG, "could not append json file while deleting a substation because: " + e.getMessage());
        }
    }

    //CHECKS_IF_EXISTS
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

    //SET_RECYCLER
    public void setUpSubstationsRecyclerView(final Context context, RecyclerView recyclerView){

        sortSubsList();
        subs_adapter = new SubstationsAdapter(subs);
        subs_adapter.setOnItemClickListener(new SubstationsAdapter.OnItemClickListener() {

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
                Intent intent = new Intent(itemView.getContext(), EditSubstation.class);
                itemView.getContext().startActivity(intent);
            }

        });

        recyclerView.setAdapter(subs_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

    }

    //UPDATE_RECYCLER
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

                    addToSubs(entry.getKey(),
                              entry.getValue().getPath(),
                              entry.getValue().getX_offset(),
                              entry.getValue().getY_offset(),
                              entry.getValue().getZ_offset(),
                              entry.getValue().getX_rotate(),
                              entry.getValue().getY_rotate(),
                              entry.getValue().getZ_rotate());
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

                ///////////////////////////
                //  Subs Validations     //
                ///////////////////////////

                boolean nameIsValid(String name){
                    return !name.equals("");
                }

                boolean pathIsValid(String path){
                    return !path.equals("");
                }

                //////////////////
                //  Subs list   //
                //////////////////

                //ADD
                public static void addToSubs(String name, String path, String xo, String yo, String zo, String xr, String yr, String zr){
                    subs.add(new Substation(name, path, xo, yo, zo, xr, yr, zr));
                    sortSubsList();
                    subs_adapter.notifyDataSetChanged();
                }

                //GET
                public static Substation getFromSubs(){
                    return subs.get(pos);
                }

                //REMOVE
                public static void removeFromSubs(){
                    subs.remove(pos);
                    sortSubsList();
                    subs_adapter.notifyItemRemoved(pos);
                    subs_adapter.notifyDataSetChanged();
                }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////
    //  Rolls     //
    ////////////////

    private static String rolls_Json_Path = Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + File.separator + "rolls.json";
    File rollsJsonFile = new File(rolls_Json_Path);
    Map<String, Roll> rollsMap = new HashMap<>();;
    TypeReference<HashMap<String, Roll>> rollTypeRef = new TypeReference<HashMap<String, Roll>>() {};
    static private ArrayList<Roll> rolls = new ArrayList<>();
    static private RollsAdapter rolls_adapter;
    private static int rolls_pos;

    //GET_ROLLS_JSON_PATH
    static public String getRollsJsonPath(){
        return rolls_Json_Path;
    }

    //SORT_ROLLS_LIST
    private static void sortRollsList(){
        Collections.sort(rolls, new Comparator<Roll>() {
            @Override
            public int compare(Roll lhs, Roll rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
    }

    //CHECKS_IF_EXISTS
    public boolean isRollExists(String name) {

        try {
            if(rollsJsonFile.exists()){
                rollsMap = objectMapper.readValue(rollsJsonFile, rollTypeRef);

                if(rollsMap.containsKey(name)){
                    return true;
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "problem with checking if roll's name already exists: " + e.getMessage());
        }

        return false;
    }

    //ADD
    public void addRollToDatabase(Roll given_roll){

        File rollsJsonFile = new File(rolls_Json_Path);

        // writing to a json file
        Roll roll = given_roll;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            if(rollsJsonFile.exists()){
                rollsMap = objectMapper.readValue(rolls_Json_Path, rollTypeRef);
                if(rollsMap.containsKey(roll.getName())){
                    Toast.makeText(getApplicationContext(),getString(R.string.TOAST_roll_named)+ " " + roll.getName() + " " + getString(R.string.TOAST_already_exists),Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            rollsMap.put(roll.getName(), roll);
            objectMapper.writeValue(new File(rolls_Json_Path), rollsMap);
        } catch (IOException e) {
            Log.d(TAG, "could not append rolls json file because: " + e.getMessage());
        }
    }

    //SET_RECYCLER
    public void setUpRollsRecyclerView(final Context context, RecyclerView recyclerView){

        sortRollsList();
        rolls_adapter = new RollsAdapter(rolls);
//        adapter.setOnItemClickListener(new SubstationsAdapter.OnItemClickListener() {
//
//            //Click a substation from substations list
//            @Override
//            public void onItemClick(View itemView, int position) {
//
//                path = subs.get(position).getPath();
//
//                if(path == ""){
//                    Toast.makeText(itemView.getContext(),getString(R.string.TOAST_upload_las),Toast.LENGTH_SHORT).show();
//                } else if(!path.endsWith(".las") ){
//                    Toast.makeText(itemView.getContext(),getString(R.string.TOAST_support_las),Toast.LENGTH_SHORT).show();
//                } else {
//                    String nodeName = subs.get(position).getName();
//                    String riskAreaJsonPath = Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + File.separator + nodeName + ".json";
//                    Log.d(TAG, "risk area path is: " + riskAreaJsonPath);
//                    updateRiskAreaJsonPath(riskAreaJsonPath);
//                    Intent intent = new Intent(itemView.getContext(), MainUnityActivity.class);
//                    itemView.getContext().startActivity(intent);
//                }
//            }

//            // click edit button on a substation
//            @Override
//            public void onButtonClick(View itemView, int position) {
//                pos = position;
//                Intent intent = new Intent(itemView.getContext(), EditSubstation.class);
//                itemView.getContext().startActivity(intent);
//            }

//        });

        recyclerView.setAdapter(rolls_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

    }

    //UPDATE_RECYCLER
    public void updateRollsRecyclerView(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        File json = new File(rolls_Json_Path);

        if(json != null){
            Map<String, Roll> rollsMap = new HashMap<>();

            try {

                TypeReference<HashMap<String, Roll>> rollTypeRef = new TypeReference<HashMap<String, Roll>>() {};
                rollsMap = objectMapper.readValue(json, rollTypeRef);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if(rollsMap.size() >= 0){
                rolls.clear();

                for (Map.Entry<String,Roll> entry : rollsMap.entrySet()){

                    addToRolls(entry.getKey());
                }

            } else{
                Log.d(TAG,"rolls Json file is empty");
            }

        } else{
            Log.d(TAG,"Cannot find rolls json file");
            removeFromRolls();
        }

        if(rolls.size() > 0)
        {
            RollsActivity.hideEmptyListText();
        } else {
            RollsActivity.displayEmptyListText();
        }
    }

                ////////////////////////////
                //  Rolls Validations     //
                ////////////////////////////

                boolean rollIsValid(String name){
                    return !name.equals("");
                }

                /////////////////////
                //  Rolls list     //
                /////////////////////

                //ADD
                public static void addToRolls(String name){
                    rolls.add(new Roll(name));
                    sortRollsList();
                    rolls_adapter.notifyDataSetChanged();
                }

                //GET
                public static Roll getFromRolls(){
                    return rolls.get(rolls_pos);
                }

                //REMOVE
                public static void removeFromRolls(){
                    rolls.remove(rolls_pos);
                    sortRollsList();
                    rolls_adapter.notifyItemRemoved(rolls_pos);
                    rolls_adapter.notifyDataSetChanged();
                }



}
