package com.developmentontheedge.sql.model;

public class AstOrderedSetAggregate extends SimpleNode
{

    public AstOrderedSetAggregate(int id)
    {
        super(id);
    }

    public AstOrderedSetAggregate(AstFunNode funNode, AstWithinGroup withinGroup)
    {
        this(0);
        addChild(funNode);
        	addChild(withinGroup);
    }
}
