package com.developmentontheedge.sql.model;

public class AstWithListElement extends SimpleNode
{
    public AstWithListElement(int id)
    {
        super(id);
    }

    @Override
    public String getChildrenDelimiter(SimpleNode prev, SimpleNode next)
    {
        	return next instanceof AstNestedQuery ? "AS" : null;
    }
}