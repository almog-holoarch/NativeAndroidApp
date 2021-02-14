package com.unity.mynativeapp;

public class Substation {

    private String name;
    private String path;
    private String x_offset, y_offset, z_offset, x_rotate, y_rotate, z_rotate;

    public Substation(){ }

    public Substation(String n, String p, String xo, String yo, String zo, String xr, String yr, String zr){
        name = n;
        path = p;

        x_offset = xo;
        y_offset = yo;
        z_offset = zo;

        x_rotate = xr;
        y_rotate = yr;
        z_rotate = zr;
    }

    public String getName(){
        return name;
    }

    public String getPath(){
        return path;
    }

    public String getX_offset() {return x_offset;}

    public String getY_offset() {return y_offset;}

    public String getZ_offset() {return z_offset;}

    public String getX_rotate() {return x_rotate;}

    public String getY_rotate() {return y_rotate;}

    public String getZ_rotate() {return z_rotate;}

    public void setName(String n){
        name = n;
    }

    public void setPath(String p){
        path = p;
    }

}

