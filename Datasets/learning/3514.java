package com.developmentontheedge.sql.model;

public class AstBeDbmsCase extends SimpleNode
{
    public AstBeDbmsCase(int id)
    {
        super(id);
        this.nodePrefix = "CASE";
    }
	}
