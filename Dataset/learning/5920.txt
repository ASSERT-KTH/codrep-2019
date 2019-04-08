package com.developmentontheedge.sql.model;

public class AstWith extends SimpleNode
{

    public AstWith(int id)
    {
        super(id);
        this.nodePrefix = "WITH";
        this.childrenDelimiter = ",";
    }

    public void setRecursion (boolean rec)
    {
        this.nodePrefix = rec ? "WITH RECURSIVE" : "WITH";
    }

}
