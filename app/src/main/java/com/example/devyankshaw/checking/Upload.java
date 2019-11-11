package com.example.devyankshaw.checking;

import com.google.firebase.database.Exclude;

public class Upload {
    private String name;
    private String email;
    private String number;
    private String password;
    private String uKey;

    public Upload() {
        //empty constructor needed
    }

    public Upload(String name, String email, String number, String password){
        this.name = name;
        this.email = email;
        this.number = number;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Exclude
    public String getuKey() {
        return uKey;
    }

    @Exclude
    public void setuKey(String uKey) {
        this.uKey = uKey;
    }
}
