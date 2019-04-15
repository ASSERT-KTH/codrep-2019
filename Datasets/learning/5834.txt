package com.developmentontheedge.sql.model;

public class AstValues extends SimpleNode
{
    public AstValues(int id)
    {
        super(id);
        this.nodePrefix = "VALUES";
        this.childrenDelimiter =",";
    }
}
