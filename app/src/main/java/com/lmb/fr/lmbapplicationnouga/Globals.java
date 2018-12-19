package com.lmb.fr.lmbapplicationnouga;

public class Globals{
    private static Globals instance;

    // Global variable
    private String PhoneNumber;

    // Restrict the constructor from being instantiated
    private Globals(){}

    public void setData(String d){
        this.PhoneNumber=d;
    }
    public String getData(){
        return this.PhoneNumber;
    }

    public static synchronized Globals getInstance(){
        if(instance==null){
            instance=new Globals();
        }
        return instance;
    }
}