package com.se_p2.hungerbell.Model;

public class User {
    private String Name;
    private String Password;
    private String Uid;

    public User() {
    }

    public User(String name, String password,String uid) {
        Name = name;
        Password = password;
        Uid=uid;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}

