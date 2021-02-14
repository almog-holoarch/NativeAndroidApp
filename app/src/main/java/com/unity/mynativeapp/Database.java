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
    private static String x_offset, y_offset, z_offset, x_rotate, y_rotate, z_rotate;

    //GET_PATH
    static public String getPath(){
        return path;
    }

    //GET_ROTATION
    static public String getRotationX(){
        return x_rotate;
    }
    static public String getRotationY(){
        return y_rotate;
    }
    static public String getRotationZ(){
        return z_rotate;
    }

    //GET_OFFSET
    static public String getOffsetX(){
        return x_offset;
    }
    static public String getOffsetY(){
        return y_offset;
    }
    static public String getOffsetZ(){
        return z_offset;
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
                x_offset = subs.get(position).getX_offset();
                y_offset = subs.get(position).getY_offset();
                z_offset = subs.get(position).getZ_offset();
                x_rotate = subs.get(position).getX_rotate();
                y_rotate = subs.get(position).getY_rotate();
                z_rotate = subs.get(position).getZ_rotate();

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
    //  Roles     //
    ////////////////

    private static String group_json_path = Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + File.separator + "groups.json";
    File groupsJsonFile = new File(group_json_path);
    Map<String, Group> groupsMap = new HashMap<>();;
    TypeReference<HashMap<String, Group>> groupTypeRef = new TypeReference<HashMap<String, Group>>() {};
    static private ArrayList<Group> groups = new ArrayList<>();
    static private GroupsAdapter groups_adapter;
    private static int groups_pos;

    //GET_ROLLS_JSON_PATH
    static public String getRollsJsonPath(){
        return group_json_path;
    }

    //SORT_ROLLS_LIST
    private static void sortGroupsList(){
        Collections.sort(groups, new Comparator<Group>() {
            @Override
            public int compare(Group lhs, Group rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
    }

    //CHECKS_IF_EXISTS
    public boolean isRollExists(String name) {

        try {
            if(groupsJsonFile.exists()){
                groupsMap = objectMapper.readValue(groupsJsonFile, groupTypeRef);

                if(groupsMap.containsKey(name)){
                    return true;
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "problem with checking if groups's name already exists: " + e.getMessage());
        }

        return false;
    }

    //ADD
    public void addRollToDatabase(Group given_group){

        File groupsJsonFile = new File(group_json_path);

        // writing to a json file
        Group group = new Group(given_group.getName());
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            if(groupsJsonFile.exists()){
                groupsMap = objectMapper.readValue(groupsJsonFile, groupTypeRef);
                if(groupsMap.containsKey(group.getName())){
                    Toast.makeText(getApplicationContext(),getString(R.string.TOAST_group_named)+ " " + group.getName() + " " + getString(R.string.TOAST_already_exists),Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            groupsMap.put(group.getName(), group);
            objectMapper.writeValue(new File(group_json_path), groupsMap);
        } catch (IOException e) {
            Log.d(TAG, "could not append groups json file because: " + e.getMessage());
        }
    }

    //EDIT
    public void editGroupInDatabase(Group edited_group, String old_name){

        try {
            groupsMap.remove(old_name);
            groupsMap.put(edited_group.getName(), edited_group);
            objectMapper.writeValue(new File(group_json_path), groupsMap);

        } catch (IOException e) {
            Log.d(TAG, "could not append json file while editing a group because: " + e.getMessage());
        }
    }

    //REMOVE
    public void removeRollFromDatabase(String old_name) {

        try {

            if(groupsJsonFile.exists()){
                groupsMap = objectMapper.readValue(groupsJsonFile, groupTypeRef);
            }

            groupsMap.remove(old_name);
            objectMapper.writeValue(new File(group_json_path), groupsMap);

            groups_adapter.notifyItemRemoved(groups_pos);
            groups_adapter.notifyDataSetChanged();

            //TODO: implement a deletion of deleted group from ALL risk area files (each file with a sub name)

        } catch (IOException e) {
            Log.d(TAG, "could not append json file while deleting a group because: " + e.getMessage());
        }
    }

    //SET_RECYCLER
    public void setUpRollsRecyclerView(final Context context, RecyclerView recyclerView){

        sortGroupsList();
        groups_adapter = new GroupsAdapter(groups);
        groups_adapter.setOnItemClickListener(new GroupsAdapter.OnItemClickListener() {

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

            // click edit button on a group
            @Override
            public void onButtonClick(View itemView, int position) {
                groups_pos = position;
                Intent intent = new Intent(itemView.getContext(), EditGroup.class);
                itemView.getContext().startActivity(intent);
            }

        });

        recyclerView.setAdapter(groups_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

    }

    //UPDATE_RECYCLER
    public void updateRollsRecyclerView(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        File json = new File(group_json_path);

        if(json != null){
            Map<String, Group> groupsMap = new HashMap<>();

            try {

                TypeReference<HashMap<String, Group>> groupTypeRef = new TypeReference<HashMap<String, Group>>() {};
                groupsMap = objectMapper.readValue(json, groupTypeRef);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if(groupsMap.size() >= 0){
                groups.clear();

                for (Map.Entry<String, Group> entry : groupsMap.entrySet()){

                    addToGroups(entry.getKey());
                }

            } else{
                Log.d(TAG,"groups Json file is empty");
            }

        } else{
            Log.d(TAG,"Cannot find groups json file");
            removeFromGroups();
        }

        if(groups.size() > 0)
        {
            GroupsActivity.hideEmptyListText();
        } else {
            GroupsActivity.displayEmptyListText();
        }
    }

                ////////////////////////////
                //  Rolls Validations     //
                ////////////////////////////

                boolean groupIsValid(String name){
                    return !name.equals("");
                }

                /////////////////////
                //  Rolls list     //
                /////////////////////

                //ADD
                public static void addToGroups(String name){
                    groups.add(new Group(name));
                    sortGroupsList();
                    groups_adapter.notifyDataSetChanged();
                }

                //GET
                public static Group getFromGroups(){
                    return groups.get(groups_pos);
                }

                //REMOVE
                public static void removeFromGroups(){
                    groups.remove(groups_pos);
                    sortGroupsList();
                    groups_adapter.notifyItemRemoved(groups_pos);
                    groups_adapter.notifyDataSetChanged();
                }

                public Map<String, Group> getGroupsMap(){
                    try {
                        if(groupsJsonFile.exists()){
                            return objectMapper.readValue(groupsJsonFile, groupTypeRef);
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "could not append groups json file because: " + e.getMessage());
                    }

                    return null;
                }
}
