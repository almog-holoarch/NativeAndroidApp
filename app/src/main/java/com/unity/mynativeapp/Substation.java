package com.unity.mynativeapp;

public class Substation {

    private String name;
    private String path;

    public Substation(){

    }

    public Substation(String n, String p){
        name = n;
        path = p;
    }

    public String getName(){
        return name;
    }

    public String getPath(){
        return path;
    }

    public void setName(String n){
        name = n;
    }

    public void setPath(String p){
        path = p;
    }

}
