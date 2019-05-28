package com.developmentontheedge.sql.model;

public enum JoinType
{
    INNER, LEFT, RIGHT, FULL;

    public AstJoin node(String table, SimpleNode specification)
    {
        return new AstJoin(this, table, 
specification);
    }
}