package com.example.firebase_refugees_app.Utils;

public class ReadWriteRefugeeDetails {
    public String id;
    public String name;
    public String doB;
    public String gender;
    public String country;

    public ReadWriteRefugeeDetails(){}
    public ReadWriteRefugeeDetails(String id,String name, String doB, String gender, String country){
        this.id=id;
        this.name = name;
        this.doB = doB;
        this.gender = gender;
        this.country = country;
    }

}
