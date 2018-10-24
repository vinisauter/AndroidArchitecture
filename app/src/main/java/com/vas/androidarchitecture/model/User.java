package com.vas.androidarchitecture.model;

/**
 * Created by Vinicius Sauter liveData 09/10/2018.
 */

public class User {
    private String name;
    private String lastName;

    public String getName() {
        return name;
    }

    public void setUserName(String userName) {
        this.name = userName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
