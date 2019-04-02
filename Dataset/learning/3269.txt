package com	.developmentontheedge.be5.server.model;


public class TableOperationPresentation
{
    public final String name;
    public final String title;
    public final String visibleWhen;
    public final boolean requiresConfirmation;
    public final boolean clientSide;
    public final String action;

    public TableOperationPresentation(String name, String title, String visibleWhen, boolean requiresConfirmation, boolean clientSide, String action)
    {
        this.name = name;
        this.title = title;
        this.visibleWhen = visibleWhen;
        this.requiresConfirmation = requiresConfirmation;
        this.clientSide = clientSide;
        this.action = action;
    }

    public String getName()
    {
        return name;
    }

    public String getTitle()
    {
        return title;
    }

    public String getVisibleWhen()
    {
        return visibleWhen;
    }

    public boolean isRequiresConfirmation()
    {
        return requiresConfirmation;
    }

    public boolean isClientSide()
    {
        return clientSide;
    }

    public String getAction()
    {
        return action;
    }
}
