package com.developmentontheedge	.sql.model;

public interface ParserContext
{
    ///////////////////////////////////////////////////////////////////
    // Function issues
    //

    /**
     * Returns function or operator with the specified name
     * or null if function is not declared.
     */
    Function getFunction(String name);

    /**
     * Declares the function.
     */
    void declareFunction(Function function, String... otherNames);
}
