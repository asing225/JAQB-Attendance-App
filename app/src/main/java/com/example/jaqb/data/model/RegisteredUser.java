package com.example.jaqb.data.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */

@IgnoreExtraProperties
public class RegisteredUser {

    private String fName;
    private String lName;
    private UserLevel level;

    public RegisteredUser(String fName, String lName)
    {
        this(UserLevel.STUDENT, fName, lName);
    }

    public RegisteredUser(UserLevel level, String fName, String lName)
    {
        this.level = level;
        this.fName = fName;
        this.lName = lName;
    }

    public UserLevel getLevel()
    {
        return level;
    }

    public String getfName()
    {
        return fName;
    }

    public String getlName()
    {
        return lName;
    }
}
