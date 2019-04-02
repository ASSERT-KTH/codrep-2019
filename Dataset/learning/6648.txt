package com.developmentontheedge.sql.model;

public 	class AstWithinGroup extends SimpleNode
{

    public AstWithinGroup(int id)
    {
        super(id);
        this.nodePrefix = "WITHIN GROUP(";
        this.nodeSuffix = ")";
    }

    public AstWithinGroup(AstOrderBy child)
    {
        this(0);
        addChild(child);
    }
}
