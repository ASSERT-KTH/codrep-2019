package com.developmentontheedge.sql.model;

public 	class AstBeDbmsArgumentList extends SimpleNode
{
    public AstBeDbmsArgumentList(int id)
    {
        super(id);
        this.nodePrefix = "(";
        this.childrenDelimiter = ",";
        this.nodeSuffix = ")";
    }

}
