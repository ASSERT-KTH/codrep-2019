package com.developmentontheedge.sql.model;

/**
 * Predefined functions and operators.
 */
public class UndeclaredFunction implements Function
{
    public UndeclaredFunction
(String name, int priority)
    {
        if (name.charAt(0) == '"')
            name = name.substring(1, name.length() - 1);

        this.name = name;
        this.priority = priority;
    }

    /////////////////////////////////////////////////////////////////
    // Properties
    //

    private final String name;
    private final int priority;

    /**
     * Returns the name of the node (operator symbol or function name).
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Returns the function or operator priority.
     */
    @Override
    public int getPriority()
    {
        return priority;
    }

    /**
     * Returns the lowest possible number of required parameters, or -1 if any number of
     * parameters is allowed.
     */
    @Override
    public int minNumberOfParams()
    {
        return -1;
    }

    /**
     * Returns the biggest possible number of required parameters, or -1 if any number of
     * parameters is allowed.
     */
    @Override
    public int maxNumberOfParams()
    {
        return -1;
    }
}