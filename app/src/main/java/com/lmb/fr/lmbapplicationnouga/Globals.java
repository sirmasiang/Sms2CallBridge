package com.lmb.fr.lmbapplicationnouga;

public class Globals{
    private static Globals instance;

    // Global variable
    private String PhoneNumber;
    private String GroupName;
    public String id;
    public String title;

    @Override
    public String toString() {
        return title;
    }

    public String getId() {
        return id;
    }

    // Restrict the constructor from being instantiated
    public Globals(){}

    public void setData(String d){
        this.PhoneNumber=d;
    }

    public String getData(){
        return this.PhoneNumber;
    }

    public void setDatag(String g){
        this.GroupName=g;
    }

    public String getDatag(){
        return this.GroupName;
    }

    public static synchronized Globals getInstance(){
        if(instance==null){
            instance=new Globals();
        }
        return instance;
    }
}