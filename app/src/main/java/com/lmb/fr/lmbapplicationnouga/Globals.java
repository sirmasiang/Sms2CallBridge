package com.lmb.fr.lmbapplicationnouga;

public class Globals{
    private static Globals instance;

    // Global variable
    private String PhoneNumber;
    private String GroupName;
    private String IntratoneToken = "eyJhbGciOiJIUzUxMiJ9.eyJpbmRpY2F0aWYiOiIzMyIsImlkIjoiNTA4NDc3NSIsInRlbCI6IjA3ODM4OTQ3NDQiLCJjb2RlIjoiOTk0MSIsImRldmljZV9pZCI6IjliMmE4MmI0NDQ2Yjc0NDkiLCJkZXZpY2UiOiJNb3Rvcm9sYSBtb3RvIGcoNTApIiwiZXhwIjoxNzkzMzY4MzI2LCJpYXQiOjE3OTI3NTk5MjYsImp0aSI6IjZhZGI1ODc2ODY1ODI0LjQyNDIxODc5IiwibmJmIjoxNzkyNzU5OTI2fQ.RkznqeQPmTBRCv0NZv2Tu8DI9HIGWLNeP_R20ZBItpBM1ICc6XcFSoZMwFZ2gI6Y1w16i8DtvmbIZ-HD878oOQ";
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