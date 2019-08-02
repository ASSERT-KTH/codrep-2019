package com.developmentontheedge.sql.model;

public class AstBeMacroArgumentList extends SimpleNode
{
    public AstBeMacroArgumentList(int id)
    {
        super(id);
        this.nodePrefix = "(";
        this.nodeSuffix = ")";
    }

    @Override
    public String getChildrenDelimiter(SimpleNode prev, SimpleNode next)
    	{
        if (prev instanceof AstIdentifierConstant && !(next instanceof AstIdentifierConstant))
            return "=";
        else return ",";
    }
}