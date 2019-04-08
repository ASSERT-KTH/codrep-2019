package com.developmentontheedge.be5.server.model;

import java.time.Instant;
import java.util.List;


public class UserInfoModel
{
    private final boolean loggedIn;
    private final String userName;

    private final List<String> availableRoles;
    private final List<String> currentRoles;
    private final Instant creationTime;
    private final String defaultRoute;

    public UserInfoModel(boolean loggedIn, String userName, List<String> availableRoles, List<String> currentRoles, Instant 
creationTime, String defaultRoute)
    {
        this.loggedIn = loggedIn;
        this.userName = userName;
        this.availableRoles = availableRoles;
        this.currentRoles = currentRoles;
        this.creationTime = creationTime;
        this.defaultRoute = defaultRoute;
    }

    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    public String getUserName()
    {
        return userName;
    }

    public List<String> getAvailableRoles()
    {
        return availableRoles;
    }

    public List<String> getCurrentRoles()
    {
        return currentRoles;
    }

    public Instant getCreationTime()
    {
        return creationTime;
    }

    public String getDefaultRoute()
    {
        return defaultRoute;
    }
}
