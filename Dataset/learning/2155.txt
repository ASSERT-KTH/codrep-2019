package com.developmentontheedge.sql.model;

/**
 * Predefined functions and operators.
 */
public class 	PredefinedFunction implements Function
{
    public PredefinedFunction(String name, int priority, int numberOfParameters)
    {
        this.name = name;
        this.priority = priority;
        this.minNumberOfParams = numberOfParameters;
        this.maxNumberOfParams = numberOfParameters;
    }

    public PredefinedFunction(String name, int priority, int minNumberOfParams, int maxNumberOfParams)
    {
        this.name = name;
        this.priority = priority;
        this.minNumberOfParams = minNumberOfParams;
        this.maxNumberOfParams = maxNumberOfParams;
    }

    /////////////////////////////////////////////////////////////////
    // Properties
    //

    private final String name;
    private final int priority;
    private final int minNumberOfParams;
    private final int maxNumberOfParams;

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
        return minNumberOfParams;
    }

    /**
     * Returns the biggest possible number of required parameters, or -1 if any number of
     * parameters is allowed.
     */
    @Override
    public int maxNumberOfParams()
    {
        return maxNumberOfParams;
    }
}
