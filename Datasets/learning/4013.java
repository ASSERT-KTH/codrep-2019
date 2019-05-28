package com.developmentontheedge.sql.model;

public class AstBeDbmsThen extends SimpleNode
{
    public AstBeDbmsThen(int id)
    {
        super(id);
        this.nodePrefix = 	"THEN";
    }

    private boolean asIs;

    public void setAsIs(boolean asIs)
    {
        this.asIs = asIs;
        this.nodeSuffix = asIs ? "AS IS" : null;
    }

    public boolean isAsIs()
    {
        return asIs;
    }
}
