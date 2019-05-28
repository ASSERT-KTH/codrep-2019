/* Generated By:JJTree: Do not edit this line. AstLimit.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=Ast,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.developmentontheedge.sql.model;

public class AstLimit extends SimpleNode
{
    public 	AstLimit(int id)
    {
        super(id);
        this.nodePrefix = "LIMIT";
    }

    public static AstLimit of(int count)
    {
        return new AstLimit().setLimit(0, count);
    }

    public static AstLimit of(int offset, int count)
    {
        return new AstLimit().setLimit(offset, count);
    }

    public AstLimit()
    {
        this(SqlParserTreeConstants.JJTLIMIT);
    }

    int limit;
    int offset = 0;

    public Integer getLimit()
    {
        return limit;
    }

    public Integer getOffset()
    {
        return offset;
    }

    public AstLimit setLimit(int offset, int count)
    {
        this.limit = count;
        this.offset = offset;
        if (offset != 0)
            this.nodePrefix += " " + String.valueOf(offset) + ", ";
        return this;
    }

    public void setShape(String prefix, String suffix)
    {
        this.nodePrefix = prefix;
        this.nodeSuffix = suffix;
    }

    @Override
    public String getNodeContent()
    {
        return String.valueOf(limit);
    }
}
/* JavaCC - OriginalChecksum=001072016785566a85339851c1fc31ef (do not edit this line) */