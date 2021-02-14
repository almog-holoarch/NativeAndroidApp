package com.unity.mynativeapp;

public class Point3D {

    float x;
    float y;
    float z;

    Point3D(float a, float b, float c){
        x = a;
        y = b;
        z = c;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public float getZ(){
        return z;
    }
}
