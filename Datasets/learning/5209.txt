package com.developmentontheedge.sql.model;

public class AstBeDbmsWhen extends SimpleNode
{
    public AstBeDbmsWhen(int id)
    {
        super(id);
        this.nodePrefix = "WHEN" ;
        this.childrenDelimiter = ",";
    }

}
