package com.developmentontheedge.sql.model;

public class AstBeMacro extends SimpleNode
{
    public AstBeMacro(int id)
    {
        super(id);
        this.nodeSuffix = "END";
    }

    private String functionName;

    public String getFunctionName()
    {
        return functionName;
    }

    public void setFunctionName(String function)
    {
        this.functionName = function;
        this.nodePrefix 	= "MACRO " + this.functionName;
    }

}
