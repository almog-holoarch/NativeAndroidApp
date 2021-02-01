package com.unity.mynativeapp;

public class Group {

    private String name;

    public Group(){ }

    public Group(String n){
        name = n;
    }

    public String getName(){
        return name;
    }

    public void setName(String n){
        name = n;
    }

}
